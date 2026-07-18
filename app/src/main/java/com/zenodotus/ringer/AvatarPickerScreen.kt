package com.zenodotus.ringer

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun AvatarPickerScreen(
    onDismiss: () -> Unit,
    onAvatarChosen: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val tempUri = remember { generateTempUri(context) }

    // pick from gallery
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onAvatarChosen(uri)
        }
    }

    // take photo
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onAvatarChosen(tempUri)
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,  // hier je ondoorzichtige witte achtergrond
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text("Kies je avatar", fontSize = 22.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = { galleryPicker.launch("image/*") }) {
                    Text("Kies uit galerij")
                }

                Button(onClick = { cameraLauncher.launch(tempUri) }) {
                    Text("Neem foto")
                }
            }
        }
    }
}

fun generateTempUri(context: Context): Uri {
    val file = File.createTempFile(
        "avatar_",      // prefix
        ".png",         // suffix
        context.cacheDir // locatie: interne cache
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", // authority in Manifest
        file
    )
}
