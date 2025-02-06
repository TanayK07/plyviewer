// app/src/main/java/com/example/plyviewer/ui/ControlPanelDropdown.kt
package com.example.plyviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.plyviewer.MyRenderer

@Composable
fun ControlPanelDropdown(myRenderer: MyRenderer?) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(8.dp)) {
        Button(onClick = { expanded = !expanded }) {
            Text(text = if (expanded) "Hide Controls" else "Show Controls")
        }
        if (expanded && myRenderer != null) {
            Column(
                modifier = Modifier
                    .background(Color(0xAA000000))
                    .padding(8.dp)
                    .width(300.dp),
                horizontalAlignment = Alignment.Start
            ) {
                var modelRotation by remember { mutableStateOf(myRenderer.modelAngle) }
                Text("Model Rotation: ${"%.1f".format(modelRotation)}°", color = Color.White)
                Slider(
                    value = modelRotation,
                    onValueChange = {
                        modelRotation = it
                        myRenderer.modelAngle = it
                    },
                    valueRange = 0f..360f
                )
                var lightAzimuth by remember { mutableStateOf(myRenderer.lightAzimuth) }
                Text("Light Azimuth: ${"%.1f".format(lightAzimuth)}°", color = Color.White)
                Slider(
                    value = lightAzimuth,
                    onValueChange = {
                        lightAzimuth = it
                        myRenderer.lightAzimuth = it
                    },
                    valueRange = -180f..180f
                )
                var lightElevation by remember { mutableStateOf(myRenderer.lightElevation) }
                Text("Light Elevation: ${"%.1f".format(lightElevation)}°", color = Color.White)
                Slider(
                    value = lightElevation,
                    onValueChange = {
                        lightElevation = it
                        myRenderer.lightElevation = it
                    },
                    valueRange = -90f..90f
                )
            }
        }
    }
}
