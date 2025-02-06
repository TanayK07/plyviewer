// app/src/main/java/com/example/plyviewer/MainActivity.kt
package com.example.plyviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.viewinterop.AndroidView
import com.example.plyviewer.ui.AnnotationOverlay
import com.example.plyviewer.ui.theme.PlyViewerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlyViewerTheme {
                val annotationViewModel: AnnotationViewModel = hiltViewModel()
                Box {
                    AndroidView(factory = { context ->
                        MyGLSurfaceView(context)
                    })
                    AnnotationOverlay(viewModel = annotationViewModel)
                }
            }
        }
    }
}
