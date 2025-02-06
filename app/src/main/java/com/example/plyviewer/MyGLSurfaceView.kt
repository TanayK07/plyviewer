package com.example.plyviewer

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyRenderer

    init {
        // Request an OpenGL ES 3.0 context.
        setEGLContextClientVersion(3)
        renderer = MyRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}
