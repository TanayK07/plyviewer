package com.example.plyviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.plyviewer.AnnotationViewModel
import com.example.plyviewer.data.AnnotationEntity
import kotlinx.coroutines.launch

@Composable
fun AnnotationOverlay(viewModel: AnnotationViewModel) {
    var annotationMode by remember { mutableStateOf(false) }
    var startPoint by remember { mutableStateOf<Offset?>(null) }
    var currentRect by remember { mutableStateOf<Rect?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(annotationMode) {
            if (annotationMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startPoint = offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        startPoint?.let { start ->
                            val current = start + dragAmount
                            currentRect = Rect(
                                left = minOf(start.x, current.x),
                                top = minOf(start.y, current.y),
                                right = maxOf(start.x, current.x),
                                bottom = maxOf(start.y, current.y)
                            )
                        }
                    },
                    onDragEnd = {
                        currentRect?.let { rect ->
                            scope.launch {
                                viewModel.addAnnotation(
                                    AnnotationEntity(
                                        type = "default", // You can extend this to allow different types.
                                        x = rect.left,
                                        y = rect.top,
                                        width = rect.width,
                                        height = rect.height
                                    )
                                )
                            }
                        }
                        startPoint = null
                        currentRect = null
                    },
                    onDragCancel = {
                        startPoint = null
                        currentRect = null
                    }
                )
            }
        }
        .drawBehind {
            currentRect?.let { rect ->
                drawRect(
                    color = Color(0x55FF0000),
                    topLeft = Offset(rect.left, rect.top),
                    size = androidx.compose.ui.geometry.Size(rect.width, rect.height)
                )
            }
        }
    ) {
        // Toggle button.
        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
        ) {
            Button(onClick = { annotationMode = !annotationMode }) {
                Text(text = if (annotationMode) "Exit Annotation" else "Enter Annotation")
            }
        }
    }
}
