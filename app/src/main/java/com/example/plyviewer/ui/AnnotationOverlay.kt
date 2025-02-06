package com.example.plyviewer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plyviewer.AnnotationViewModel
import com.example.plyviewer.data.AnnotationEntity
import kotlin.math.abs
import kotlin.math.min

@Composable
fun AnnotationOverlay(viewModel: AnnotationViewModel) {
    var annotationMode by remember { mutableStateOf(false) }
    var currentRect by remember { mutableStateOf<Rect?>(null) }

    // Listen to the DB's stored annotations
    val annotations by viewModel.annotations.collectAsState()

    // For shape and label
    var shapeType by remember { mutableStateOf("rect") }
    var labelField by remember { mutableStateOf("") }
    val labelText = labelField.ifBlank { "No Label" }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1) If annotationMode is ON, intercept single-drag gestures to create shapes
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
                                        // Insert annotation with the label
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
            // 2) Otherwise just draw existing shapes, so pinch/pan/rotate pass through
            Box(modifier = Modifier.fillMaxSize()) {
                DrawAnnotations(annotations, null, shapeType)
            }
        }

        // 3) UI Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Toggle annotation mode
            Button(onClick = { annotationMode = !annotationMode }) {
                Text(if (annotationMode) "Exit Annotation" else "Enter Annotation")
            }
            Spacer(Modifier.height(8.dp))

            // Shape type toggles
            Row {
                Button(onClick = { shapeType = "line" }) { Text("Line") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { shapeType = "rect" }) { Text("Rect") }
            }
            Spacer(Modifier.height(8.dp))

            // Label input (applies to next annotation)
            OutlinedTextField(
                value = labelField,
                onValueChange = { labelField = it },
                label = { Text("Annotation Label") }
            )
            Spacer(Modifier.height(8.dp))

            // Reset button
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
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        // Draw existing from DB
        annotations.forEach { ann ->
            if (ann.shape == "line") {
                // interpret (x, y, width, height) as start->end
                drawLine(
                    color = Color.Red.copy(alpha = 0.5f),
                    start = Offset(ann.x, ann.y),
                    end = Offset(ann.width, ann.height),
                    strokeWidth = 8f
                )
                // label in the middle
                val midX = (ann.x + ann.width) / 2f
                val midY = (ann.y + ann.height) / 2f
                drawContext.canvas.nativeCanvas.drawText(
                    ann.label,
                    midX,
                    midY,
                    textPaint
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
                // label in center
                val midX = left + w / 2f
                val midY = top + h / 2f
                drawContext.canvas.nativeCanvas.drawText(
                    ann.label,
                    midX,
                    midY,
                    textPaint
                )
            }
        }
        // In-progress shape
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
