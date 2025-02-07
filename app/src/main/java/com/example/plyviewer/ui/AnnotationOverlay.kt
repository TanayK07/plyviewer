package com.example.plyviewer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.plyviewer.AnnotationViewModel
import com.example.plyviewer.data.AnnotationEntity
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope

@Composable
fun AnnotationOverlay(viewModel: AnnotationViewModel) {
    // Are we in "annotation mode" or not?
    var annotationMode by remember { mutableStateOf(false) }

    // The rect the user just dragged, waiting for label & submit
    var pendingRect by remember { mutableStateOf<Rect?>(null) }

    // When user is about to "submit" the shape, we store the shape type and label here
    var shapeType by remember { mutableStateOf("rect") }
    var pendingLabel by remember { mutableStateOf("") } // label for the shape in progress

    // Observe existing DB annotations
    val annotations by viewModel.annotations.collectAsState()

    // For tapping existing shapes
    var selectedAnnotation by remember { mutableStateOf<AnnotationEntity?>(null) }
    var editLabel by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1) If annotation mode is ON, intercept single-finger drags
        if (annotationMode) {
            // Single-finger drag => store in `pendingRect`. But we do NOT add to DB yet.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset: Offset ->
                                // Start new shape
                                pendingRect = Rect(offset, offset)
                                // Clear label if user wants a fresh shape
                                pendingLabel = ""
                            },
                            onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                change.consume() // block this pointer
                                pendingRect = pendingRect?.let { rect ->
                                    rect.copy(
                                        right = rect.right + dragAmount.x,
                                        bottom = rect.bottom + dragAmount.y
                                    )
                                }
                            },
                            onDragEnd = {
                                // End of drag. The shape is now in `pendingRect`.
                                // We'll wait for user to press "Add Shape" to finalize.
                            },
                            onDragCancel = {
                                pendingRect = null
                            }
                        )
                    }
            ) {
                DrawAnnotations(
                    annotations = annotations,
                    pendingRect = pendingRect,
                    shapeType = shapeType,
                    selectedAnnotation = selectedAnnotation
                )
            }
        } else {
            // Not in annotation mode => pass multi-touch to GLSurfaceView
            // We only do single-tap detection for selecting existing shapes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(annotations) {
                        detectTapOnAnnotations(
                            annotations = annotations,
                            onTapAnnotation = { ann ->
                                selectedAnnotation = ann
                                editLabel = ann.type // if "type" is used as label
                            }
                        )
                    }
            ) {
                DrawAnnotations(
                    annotations = annotations,
                    pendingRect = null,
                    shapeType = shapeType,
                    selectedAnnotation = selectedAnnotation
                )
            }
        }

        // 2) The bottom panel: shape toggles, label text, submit button, etc.
        Box(modifier = Modifier.fillMaxSize()) {
            AnnotationPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                annotationMode = annotationMode,
                onToggleAnnotationMode = {
                    annotationMode = !annotationMode
                    // If turning OFF annotation mode, discard pendingRect
                    if (!annotationMode) pendingRect = null
                },
                shapeType = shapeType,
                onShapeSelected = { shapeType = it },

                // For new shape
                pendingLabel = pendingLabel,
                onPendingLabelChange = { pendingLabel = it },
                onAddShape = {
                    // If we have a pendingRect, create the annotation in DB
                    val r = pendingRect
                    if (r != null) {
                        val w = abs(r.width)
                        val h = abs(r.height)
                        if (w > 10f || h > 10f) {
                            viewModel.addAnnotation(
                                AnnotationEntity(
                                    // adapt to your entity fields
                                    id = 0,
                                    type = pendingLabel.ifBlank { "No Label" },
                                    x = r.left,
                                    y = r.top,
                                    width = r.right,
                                    height = r.bottom
                                ).copy(type = pendingLabel)
                            )
                        }
                    }
                    // Clear the pending shape & label
                    pendingRect = null
                    pendingLabel = ""
                },

                onReset = { viewModel.clearAllAnnotations() },

                // For editing an existing shape
                selectedAnnotation = selectedAnnotation,
                editLabel = editLabel,
                onEditLabelChange = { editLabel = it },
                onSaveSelectedAnnotation = {
                    if (selectedAnnotation != null) {
                        viewModel.updateAnnotation(
                            selectedAnnotation!!.copy(type = editLabel)
                        )
                        selectedAnnotation = null
                    }
                },
                onCloseSelectedAnnotation = { selectedAnnotation = null }
            )
        }
    }
}

/**
 * Draw existing DB shapes + a "pendingRect" if user is mid-annotation,
 * and highlight a selected shape if any.
 */
@Composable
fun DrawAnnotations(
    annotations: List<AnnotationEntity>,
    pendingRect: Rect?,
    shapeType: String,
    selectedAnnotation: AnnotationEntity?
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw existing shapes
        annotations.forEach { ann ->
            val isSelected = (ann.id == selectedAnnotation?.id)
            val color = if (isSelected) Color.Yellow.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)

            // If ann.type == "line", interpret as a line
            // If ann.type == "rect", interpret as rectangle
            // Or if "type" is label, maybe you store shape differently
            if (ann.type.contains("line", ignoreCase = true)) {
                drawLine(
                    color = color,
                    start = Offset(ann.x, ann.y),
                    end = Offset(ann.width, ann.height),
                    strokeWidth = if (isSelected) 10f else 6f
                )
            } else {
                // treat as rect
                val left = min(ann.x, ann.width)
                val top = min(ann.y, ann.height)
                val w = abs(ann.width - ann.x)
                val h = abs(ann.height - ann.y)
                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
            }
        }

        // If user is dragging a shape (pendingRect), show it in a different color
        pendingRect?.let { r ->
            val c = Color.Blue.copy(alpha = 0.4f)
            if (shapeType == "line") {
                drawLine(
                    color = c,
                    start = Offset(r.left, r.top),
                    end = Offset(r.right, r.bottom),
                    strokeWidth = 6f
                )
            } else {
                val left = min(r.left, r.right)
                val top = min(r.top, r.bottom)
                val w = abs(r.width)
                val h = abs(r.height)
                drawRect(
                    color = c,
                    topLeft = Offset(left, top),
                    size = Size(w, h)
                )
            }
        }
    }
}

/**
 * detectTapOnAnnotations: A simple approach that uses a drag gesture but
 * only checks the onDragStart as a "tap"
 */

suspend fun PointerInputScope.detectTapOnAnnotations(
    annotations: List<AnnotationEntity>,
    onTapAnnotation: (AnnotationEntity) -> Unit
) {
    detectDragGestures(
        onDragStart = { offset ->
            val tapped = findAnnotationAt(annotations, offset)
            if (tapped != null) {
                onTapAnnotation(tapped)
            }
        },
        onDrag = { change: PointerInputChange, _ ->
            change.consume() // so it doesn't do anything else
        },
        onDragEnd = {},
        onDragCancel = {}
    )
}

fun findAnnotationAt(annotations: List<AnnotationEntity>, offset: Offset): AnnotationEntity? {
    val threshold = 20f
    return annotations.firstOrNull { ann ->
        // If you store shape vs label in `ann.type`, adapt as needed
        if (ann.type.contains("line", ignoreCase = true)) {
            val dist = pointToLineDistance(offset, Offset(ann.x, ann.y), Offset(ann.width, ann.height))
            dist < threshold
        } else {
            // treat as rect
            val left = min(ann.x, ann.width)
            val top = min(ann.y, ann.height)
            val right = max(ann.x, ann.width)
            val bottom = max(ann.y, ann.height)
            offset.x in left..right && offset.y in top..bottom
        }
    }
}

// Distance from a point to a line segment
fun pointToLineDistance(p: Offset, a: Offset, b: Offset): Float {
    val abx = b.x - a.x
    val aby = b.y - a.y
    val apx = p.x - a.x
    val apy = p.y - a.y
    val abLen2 = abx * abx + aby * aby
    if (abLen2 < 1e-9) return hypot(apx, apy)
    val t = ((apx * abx) + (apy * aby)) / abLen2
    val u = when {
        t < 0f -> 0f
        t > 1f -> 1f
        else -> t
    }
    val projx = a.x + u * abx
    val projy = a.y + u * aby
    return hypot(p.x - projx, p.y - projy)
}
@Composable
fun AnnotationPanel(
    modifier: Modifier = Modifier,
    annotationMode: Boolean,
    onToggleAnnotationMode: () -> Unit,
    shapeType: String,
    onShapeSelected: (String) -> Unit,

    // For new shape
    pendingLabel: String,
    onPendingLabelChange: (String) -> Unit,
    onAddShape: () -> Unit,

    onReset: () -> Unit,

    // For selected annotation editing
    selectedAnnotation: AnnotationEntity?,
    editLabel: String,
    onEditLabelChange: (String) -> Unit,
    onSaveSelectedAnnotation: () -> Unit,
    onCloseSelectedAnnotation: () -> Unit
) {
    // We assume your code can track if there's a pendingRect externally
    // by only showing "Add Shape" if pendingRect != null.
    // Or pass a boolean "hasPendingRect" if you prefer.

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Annotation Controls",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            Row {
                Button(
                    onClick = onToggleAnnotationMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (annotationMode) Color(0xFFAA3333) else Color(0xFF008577)
                    )
                ) {
                    Text(if (annotationMode) "Exit Annotation" else "Enter Annotation")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAA5500))
                ) {
                    Text("Reset All")
                }
            }
            Spacer(Modifier.height(8.dp))

            // If in annotation mode: show shape toggles, label field, and "Add Shape" button
            if (annotationMode) {
                Row {
                    OutlinedButton(
                        onClick = { onShapeSelected("line") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (shapeType == "line") Color(0xFF5555AA) else Color.Transparent
                        )
                    ) {
                        Text("Line", color = if (shapeType == "line") Color.White else Color.Black)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onShapeSelected("rect") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (shapeType == "rect") Color(0xFF5555AA) else Color.Transparent
                        )
                    ) {
                        Text("Rect", color = if (shapeType == "rect") Color.White else Color.Black)
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Label for pending shape
                OutlinedTextField(
                    value = pendingLabel,
                    onValueChange = onPendingLabelChange,
                    label = { Text("Shape Label") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.height(8.dp))

                // "Add Shape" button to finalize the shape in DB
                Button(
                    onClick = onAddShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558855))
                ) {
                    Text("Add Shape")
                }
            }

            // If user selected an existing annotation => label editing
            selectedAnnotation?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Selected Annotation #${it.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = editLabel,
                    onValueChange = onEditLabelChange,
                    label = { Text("Edit Label") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(Modifier.height(8.dp))

                Row {
                    Button(
                        onClick = onSaveSelectedAnnotation,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF337733))
                    ) {
                        Text("Save")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onCloseSelectedAnnotation) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
