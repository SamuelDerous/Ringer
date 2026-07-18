package com.zenodotus.ringer.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.room.withTransaction
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.AppDatabase
import com.zenodotus.ringer.database.LoginUiState
import com.zenodotus.ringer.database.Trophy
import com.zenodotus.ringer.database.User
import com.zenodotus.ringer.database.UserSetting
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val application: Application,
    private val database: AppDatabase,
    private val trophyViewModel: TrophyViewModel,
    private val taskViewModel: TaskViewModel,
    private val userSettingsViewModel: UserSettingsViewModel
) : ViewModel() {

    private val argon2 = Argon2Kt()
    private val salt = "somesalt".toByteArray() // Tip: Sla dit later uniek per user op!

    private val userPrefs = UserPreferences(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // --- UI Events ---

    fun onUserNameChange(value: String) {
        _uiState.update { it.copy(userName = value, userNameError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onMasterPasswordChange(value: String) {
        _uiState.update { it.copy(masterPassword = value) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                passwordError = null,
                userNameError = null
            )
        }
    }

    fun toggleMasterField() {
        _uiState.update { it.copy(showMasterField = !it.showMasterField) }
    }

    fun updateAvatar(uri: String) {
        _uiState.update { it.copy(avatar = uri, isDialogOpen = false) }
    }

    fun setDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isDialogOpen = visible) }
    }

    // --- Business Logica ---

    fun login(navController: NavController) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val user = database.userDao().getUser(state.userName)
            if (user != null) {
                val isValid = argon2.verify(
                    Argon2Mode.ARGON2_I,
                    user.passwordHash,
                    state.password.toByteArray()
                )
                if (isValid) {
                    completeLogin(state.userName, navController)
                } else {
                    _uiState.update {
                        it.copy(
                            passwordError = "Combinatie incorrect",
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        passwordError = "Gebruiker bestaat niet",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun register(navController: NavController) {
        val state = _uiState.value

        // Validatie
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "Ongeldig e-mailadres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (database.userDao().getUsernames(state.userName) == 0) {
                val passwordHash = argon2.hash(
                    Argon2Mode.ARGON2_I,
                    state.password.toByteArray(),
                    salt,
                    5,
                    65536
                )

                database.withTransaction {
                    // Maak User
                    database.userDao().insert(
                        User(
                            state.userName,
                            state.avatar,
                            passwordHash.encodedOutputAsString(),
                            state.email
                        )
                    )
                    // Maak Settings
                    database.userSettingsDao().insert(
                        UserSetting(state.userName, false, false, false, false, 0, 0)
                    )
                    // Maak Starter Trophy
                    database.trophyDao().insert(
                        Trophy(0, state.userName, null, "cumulative", "Starter", 0)
                    )
                    // Master password indien ingevuld
                    if (state.showMasterField && state.masterPassword.isNotEmpty()) {
                        database.userDao().setMasterPassword(state.userName, state.masterPassword)
                    }
                }
                completeLogin(state.userName, navController)
            } else {
                _uiState.update {
                    it.copy(
                        userNameError = "Gebruikersnaam bestaat al",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun completeLogin(userName: String, navController: NavController) {
        userPrefs.saveUsername(userName)
        userPrefs.setLoggedIn(true)

        // Laad alle data in de andere ViewModels
        trophyViewModel.loadTrophiesForUser(userName)
        taskViewModel.loadTasks(userName)
        userSettingsViewModel.loadSettingsForMasterUser(userName)

        _uiState.update { it.copy(isLoading = false) }
        navController.navigate("main")
    }
}