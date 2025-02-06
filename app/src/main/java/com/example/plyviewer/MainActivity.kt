package com.example.plyviewer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.plyviewer.ui.AnnotationOverlay
import com.example.plyviewer.ui.ControlPanelDropdown
import com.example.plyviewer.ui.theme.PlyViewerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate - Setting content.")
        setContent {
            PlyViewerTheme {
                val annotationViewModel: AnnotationViewModel = hiltViewModel()
                var myGLSurfaceView by remember { mutableStateOf<MyGLSurfaceView?>(null) }
                Box(modifier = Modifier.fillMaxSize()) {
                    // 3D view
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            Log.d("MainActivity", "Creating MyGLSurfaceView now.")
                            MyGLSurfaceView(context).also { view ->
                                myGLSurfaceView = view
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    // Annotation overlay
                    //TEMP
                    AnnotationOverlay(viewModel = annotationViewModel)
                    // Control panel
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        ControlPanelDropdown(myRenderer = myGLSurfaceView?.getRenderer())
                    }
                }
            }
        }
    }
}
