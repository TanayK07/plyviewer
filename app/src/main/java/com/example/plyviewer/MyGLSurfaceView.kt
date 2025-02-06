package com.example.plyviewer

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyRenderer
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    init {
        setEGLContextClientVersion(3)
        renderer = MyRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                renderer.cameraDistance /= detector.scaleFactor
                // Clamp camera distance.
                if (renderer.cameraDistance < 1f) renderer.cameraDistance = 1f
                if (renderer.cameraDistance > 10f) renderer.cameraDistance = 10f
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                renderer.cameraTranslateX += distanceX / 100f
                renderer.cameraTranslateY -= distanceY / 100f
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                renderer.resetCamera()
                return true
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }
}
