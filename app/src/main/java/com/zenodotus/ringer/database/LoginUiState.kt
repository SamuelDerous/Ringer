package com.zenodotus.ringer.database

data class LoginUiState(
    // Tekstveld waarden
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val masterPassword: String = "",
    val avatar: String = "neutral.png",

    // UI Logica status
    val isLoginMode: Boolean = true,      // Schakelt tussen Login en Registratie layout
    val showMasterField: Boolean = false, // Toont/verbergt het master password veld
    val isLoading: Boolean = false,       // Voor een ProgressBar tijdens hashing/DB acties
    val isDialogOpen: Boolean = false,    // Voor de AvatarPicker

    // Foutmeldingen (null betekent geen fout)
    val userNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null      // Voor algemene fouten (bijv. "Geen internet")
)