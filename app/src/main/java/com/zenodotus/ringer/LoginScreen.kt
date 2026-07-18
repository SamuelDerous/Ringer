package com.zenodotus.ringer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.zenodotus.ringer.viewmodels.LoginViewModel


@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: LoginViewModel
) {
    // CRUCIAAL: Dit zet de Flow om in een State die Compose begrijpt
    val state by authViewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Handig voor als het keyboard omhoog komt
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isLoginMode) {
                // --- LOGIN SECTIE ---
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.userName,
                    onValueChange = { authViewModel.onUserNameChange(it) },
                    label = { Text("Gebruikersnaam") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { authViewModel.onPasswordChange(it) },
                    label = { Text("Wachtwoord") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.passwordError != null
                )

                state.passwordError?.let {
                    Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.login(navController) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Inloggen")
                    }
                }

            } else {
                // --- REGISTRATIE SECTIE ---
                Text("Registreren", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Avatar
                Image(
                    painter = rememberAsyncImagePainter(
                        if (state.avatar.startsWith("content://")) state.avatar
                        else "file:///android_asset/avatars/${state.avatar}"
                    ),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { authViewModel.setDialogVisible(true) }
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.userName,
                    onValueChange = { authViewModel.onUserNameChange(it) },
                    label = { Text("Gebruikersnaam") },
                    isError = state.userNameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                state.userNameError?.let { Text(it, color = Color.Red) }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { authViewModel.onEmailChange(it) },
                    label = { Text("E-mail") },
                    isError = state.emailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                state.emailError?.let { Text(it, color = Color.Red) }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { authViewModel.onPasswordChange(it) },
                    label = { Text("Wachtwoord") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Master Password sectie
                TextButton(onClick = { authViewModel.toggleMasterField() }) {
                    Text(if (state.showMasterField) "Verberg Master Password" else "Set Master Password")
                }

                if (state.showMasterField) {
                    OutlinedTextField(
                        value = state.masterPassword,
                        onValueChange = { authViewModel.onMasterPasswordChange(it) },
                        label = { Text("Master Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { authViewModel.register(navController) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text(if (state.showMasterField) "Registreer met Master Pass" else "Registreren")
                }
            }

            // Switch tussen Login en Registratie
            TextButton(onClick = { authViewModel.toggleMode() }) {
                Text(
                    text = if (state.isLoginMode) "Nog geen account? Registreren" else "Al een account? Inloggen",
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }

    // Avatar Picker Dialog
    if (state.isDialogOpen) {
        AvatarPickerScreen(
            onDismiss = { authViewModel.setDialogVisible(false) },
            onAvatarChosen = { uri -> authViewModel.updateAvatar(uri.toString()) }
        )
    }
}