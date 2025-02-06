package com.example.plyviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.plyviewer.ui.AnnotationOverlay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box {
                // The GLSurfaceView for rendering the PLY model.
                AndroidView(factory = { context ->
                    MyGLSurfaceView(context)
                }, modifier = Modifier.matchParentSize())

                // Compose overlay for annotations.
                AnnotationOverlay()
            }
        }
    }
}
