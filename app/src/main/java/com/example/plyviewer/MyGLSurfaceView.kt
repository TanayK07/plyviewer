package com.example.plyviewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    private val renderer: MyRenderer
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // Flags to help us know if we’re scaling or rotating at the moment.
    private var isScaling = false
    private var isRotating = false

    // Previous pointer positions for rotation.
    private var previousX0 = 0f
    private var previousY0 = 0f
    private var previousX1 = 0f
    private var previousY1 = 0f

    init {
        setEGLContextClientVersion(3)

        // Our custom renderer.
        renderer = MyRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        // Scale (Pinch) Gesture Detector
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                Log.d("MyGLSurfaceView", "onScaleBegin - Starting pinch/zoom")
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val oldDistance = renderer.cameraDistance
                // Apply scale factor (the smaller the factor, the closer you get).
                renderer.cameraDistance /= detector.scaleFactor
                // Clamp camera distance to [1f..10f]
                renderer.cameraDistance = renderer.cameraDistance.coerceIn(1f, 10f)

                Log.d("MyGLSurfaceView", "onScale - scaleFactor=${detector.scaleFactor}, cameraDistance from $oldDistance to ${renderer.cameraDistance}")
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
                Log.d("MyGLSurfaceView", "onScaleEnd - Pinch/zoom finished")
            }
        })

        // Pan & Double-tap Gesture Detector
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Only pan if not scaling or rotating.
                if (!isScaling && !isRotating) {
                    val oldX = renderer.cameraTranslateX
                    val oldY = renderer.cameraTranslateY
                    renderer.cameraTranslateX += distanceX / 100f
                    renderer.cameraTranslateY -= distanceY / 100f
                    Log.d("MyGLSurfaceView", "onScroll - Panning camera from ($oldX, $oldY) to (${renderer.cameraTranslateX}, ${renderer.cameraTranslateY})")
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                Log.d("MyGLSurfaceView", "onDoubleTap - Resetting camera")
                renderer.resetCamera()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // First pass events to scale and gesture detectors.
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isRotating = true
                    // Capture positions for the two pointers.
                    previousX0 = event.getX(0)
                    previousY0 = event.getY(0)
                    previousX1 = event.getX(1)
                    previousY1 = event.getY(1)
                    Log.d("MyGLSurfaceView", "ACTION_POINTER_DOWN - Starting rotation mode")
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // If exactly 2 pointers and we’re in rotation mode, rotate the model.
                if (event.pointerCount == 2 && isRotating && !isScaling) {
                    val angleDelta = calculateRotationAngle(event)
                    renderer.modelAngle += angleDelta
                    Log.d("MyGLSurfaceView", "ACTION_MOVE - Rotating modelAngle by $angleDelta degrees to ${renderer.modelAngle}")
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // If pointer count is dropping below 2, end rotation.
                if (event.pointerCount <= 2) {
                    isRotating = false
                    Log.d("MyGLSurfaceView", "ACTION_POINTER_UP - Exiting rotation mode")
                }
            }
        }
        return true
    }

    private fun calculateRotationAngle(event: MotionEvent): Float {
        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)

        // Current angle based on the two pointer positions.
        val currentAngle = Math.toDegrees(atan2((y1 - y0).toDouble(), (x1 - x0).toDouble())).toFloat()
        // Previous angle based on stored pointer positions.
        val prevAngle = Math.toDegrees(atan2((previousY1 - previousY0).toDouble(), (previousX1 - previousX0).toDouble())).toFloat()

        // Update stored pointer coords for next frame.
        previousX0 = x0
        previousY0 = y0
        previousX1 = x1
        previousY1 = y1

        // Compute difference, adjusting for wrap-around (e.g. 179 -> -179).
        var angleDelta = currentAngle - prevAngle
        if (angleDelta > 180) angleDelta -= 360f
        if (angleDelta < -180) angleDelta += 360f

        return angleDelta
    }

    fun getRenderer(): MyRenderer = renderer
}
