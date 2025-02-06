package com.example.plyviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnnotationOverlay() {
    var annotationMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Button to toggle annotation mode.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(onClick = { annotationMode = !annotationMode }) {
                Text(text = if (annotationMode) "Exit Annotation" else "Enter Annotation")
            }
        }
        if (annotationMode) {
            // Semi-transparent overlay.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55FF0000))
            )
        }
    }
}
