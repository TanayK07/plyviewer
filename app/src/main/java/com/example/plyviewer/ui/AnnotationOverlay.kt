package com.example.plyviewer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.plyviewer.data.AnnotationEntity
import com.example.plyviewer.AnnotationViewModel
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.OutlinedTextField
import kotlin.math.abs
import kotlin.math.min
@Composable
fun AnnotationOverlay(viewModel: AnnotationViewModel) {
    var annotationMode by remember { mutableStateOf(false) }
    var currentRect by remember { mutableStateOf<Rect?>(null) }

    // Observe
    val annotations by viewModel.annotations.collectAsState()

    // Controls
    var shapeType by remember { mutableStateOf("rect") }
    var labelField by remember { mutableStateOf("") }  // user input
    val labelText = labelField.ifBlank { "No Label" }

    Box(modifier = Modifier.fillMaxSize()) {
        if (annotationMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentRect = Rect(offset, offset)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentRect = currentRect?.let { rect ->
                                    rect.copy(
                                        right = rect.right + dragAmount.x,
                                        bottom = rect.bottom + dragAmount.y
                                    )
                                }
                            },
                            onDragEnd = {
                                currentRect?.let { r ->
                                    val w = abs(r.width)
                                    val h = abs(r.height)
                                    if (w > 10 || h > 10) {
                                        // Save annotation
                                        viewModel.addAnnotation(
                                            AnnotationEntity(
                                                shape = shapeType,
                                                label = labelText,
                                                x = r.left,
                                                y = r.top,
                                                width = r.right,
                                                height = r.bottom
                                            )
                                        )
                                    }
                                }
                                currentRect = null
                            },
                            onDragCancel = { currentRect = null }
                        )
                    }
            ) {
                DrawAnnotations(annotations, currentRect, shapeType)
            }
        } else {
            // Not in annotation mode => just draw existing
            Box(modifier = Modifier.fillMaxSize()) {
                DrawAnnotations(annotations, null, shapeType)
            }
        }

        // UI for controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(onClick = { annotationMode = !annotationMode }) {
                Text(if (annotationMode) "Exit Annotation" else "Enter Annotation")
            }
            Spacer(Modifier.height(8.dp))

            // shape type toggles
            Row {
                Button(onClick = { shapeType = "line" }) { Text("Line") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { shapeType = "rect" }) { Text("Rect") }
            }
            Spacer(Modifier.height(8.dp))

            // label input
            OutlinedTextField(
                value = labelField,
                onValueChange = { labelField = it },
                label = { Text("Annotation Label") }
            )
            Spacer(Modifier.height(8.dp))

            // reset
            Button(onClick = { viewModel.clearAllAnnotations() }) {
                Text("Reset All Annotations")
            }
        }
    }
}

@Composable
fun DrawAnnotations(
    annotations: List<AnnotationEntity>,
    currentRect: Rect?,
    shapeType: String
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // draw existing
        annotations.forEach { ann ->
            if (ann.shape == "line") {
                // interpret (x, y, width, height) as start->end
                drawLine(
                    color = Color.Red.copy(alpha = 0.5f),
                    start = Offset(ann.x, ann.y),
                    end = Offset(ann.width, ann.height),
                    strokeWidth = 8f
                )
            } else { // "rect"
                val left = min(ann.x, ann.width)
                val top = min(ann.y, ann.height)
                val w = abs(ann.width - ann.x)
                val h = abs(ann.height - ann.y)
                drawRect(
                    color = Color.Red.copy(alpha = 0.5f),
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
            }
        }

        // if user is dragging
        currentRect?.let { r ->
            if (shapeType == "line") {
                drawLine(
                    color = Color.Blue.copy(alpha = 0.5f),
                    start = Offset(r.left, r.top),
                    end = Offset(r.right, r.bottom),
                    strokeWidth = 8f
                )
            } else {
                val left = min(r.left, r.right)
                val top = min(r.top, r.bottom)
                val w = abs(r.width)
                val h = abs(r.height)
                drawRect(
                    color = Color.Blue.copy(alpha = 0.5f),
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
            }
        }
    }
}
