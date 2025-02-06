// app/src/main/java/com/example/plyviewer/ui/CameraAndLightingControls.kt
package com.example.plyviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.plyviewer.MyGLSurfaceView

@Composable
fun CameraAndLightingControls(myGLSurfaceView: MyGLSurfaceView?) {
    var modelRotation by remember { mutableStateOf(myGLSurfaceView?.getRenderer()?.modelAngle ?: 0f) }
    var lightAzimuth by remember { mutableStateOf(myGLSurfaceView?.getRenderer()?.lightAzimuth ?: 0f) }
    var lightElevation by remember { mutableStateOf(myGLSurfaceView?.getRenderer()?.lightElevation ?: 0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x88000000))
            .padding(8.dp)
    ) {
        Text("Model Rotation: ${"%.1f".format(modelRotation)}°", color = Color.White)
        Slider(
            value = modelRotation,
            onValueChange = {
                modelRotation = it
                myGLSurfaceView?.getRenderer()?.modelAngle = it
            },
            valueRange = 0f..360f
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Light Azimuth: ${"%.1f".format(lightAzimuth)}°", color = Color.White)
        Slider(
            value = lightAzimuth,
            onValueChange = {
                lightAzimuth = it
                myGLSurfaceView?.getRenderer()?.lightAzimuth = it
            },
            valueRange = -180f..180f
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Light Elevation: ${"%.1f".format(lightElevation)}°", color = Color.White)
        Slider(
            value = lightElevation,
            onValueChange = {
                lightElevation = it
                myGLSurfaceView?.getRenderer()?.lightElevation = it
            },
            valueRange = -90f..90f
        )
    }
}
