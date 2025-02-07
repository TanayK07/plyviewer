package com.example.plyviewer

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.plyviewer.PlyParser.PlyModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "MyRenderer"
    }

    // We store transform matrices for unproject
    val modelMatrix = FloatArray(16) { 0f }
    val viewMatrix = FloatArray(16) { 0f }
    val projMatrix = FloatArray(16) { 0f }

    // Camera params
    var cameraDistance = 3f
    var cameraTranslateX = 0f
    var cameraTranslateY = 0f
    var modelAngle = 0f  // For rotating the model

    // The mesh data
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: IntBuffer
    private var indexCount: Int = 0

    // This is for potential intersection
    // e.g. each vertex = [x, y, z, r, g, b, a], or we only care about x,y,z
    var vertexCount = 0
    var rawVertices: FloatArray = floatArrayOf()  // store the raw positions if needed
    var rawIndices: IntArray = intArrayOf()

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Load PLY
        try {
            val inputStream = context.assets.open("model.ply")
            val plyModel: PlyModel = PlyParser().parse(inputStream)
            vertexCount = plyModel.vertices.size / 7
            indexCount = plyModel.indices.size
            rawVertices = plyModel.vertices
            rawIndices = plyModel.indices

            // set up buffers for drawing if needed
            vertexBuffer = ByteBuffer.allocateDirect(plyModel.vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(plyModel.vertices)
                    position(0)
                }
            if (indexCount > 0) {
                indexBuffer = ByteBuffer.allocateDirect(indexCount * 4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer().apply {
                        put(plyModel.indices)
                        position(0)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading PLY", e)
        }
        // ... create your shader program, etc. ...
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Build modelMatrix from modelAngle
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, modelAngle, 0f, 1f, 0f)

        // Build viewMatrix from cameraDistance, cameraTranslateX, cameraTranslateY
        // A simple approach:
        Matrix.setLookAtM(
            viewMatrix, 0,
            cameraTranslateX, cameraTranslateY, cameraDistance,
            cameraTranslateX, cameraTranslateY, 0f,
            0f, 1f, 0f
        )

        // ... multiply out MVP, set up shaders, draw ...
        // Not shown for brevity
    }

    // -----------------------------------------------------
    //  RAY INTERSECTION  (Naive, for demonstration only)
    // -----------------------------------------------------
    data class IntersectionResult(val hit: Boolean, val point: Vec3, val normal: Vec3)

    fun intersectRay(origin: Vec3, dir: Vec3): IntersectionResult {
        // If we have rawIndices in groups of 3, each triple is one triangle
        // If your mesh is large, this is SLOW
        var closestT = Float.POSITIVE_INFINITY
        var bestHit = Vec3(0f, 0f, 0f)
        var bestNormal = Vec3(0f, 0f, 0f)
        var foundHit = false

        // For each triangle
        for (i in rawIndices.indices step 3) {
            val i0 = rawIndices[i]
            val i1 = rawIndices[i+1]
            val i2 = rawIndices[i+2]

            // Extract x,y,z from rawVertices (7 floats per vertex)
            val v0 = getVertex3D(i0)
            val v1 = getVertex3D(i1)
            val v2 = getVertex3D(i2)

            val (didHit, tVal) = intersectRayTriangle(origin, dir, v0, v1, v2)
            if (didHit && tVal in 0f..closestT) {
                closestT = tVal
                foundHit = true
                bestHit = origin + dir * tVal
                bestNormal = computeNormal(v0, v1, v2)
            }
        }
        return if (foundHit) {
            IntersectionResult(true, bestHit, bestNormal)
        } else {
            IntersectionResult(false, Vec3(0f,0f,0f), Vec3(0f,0f,0f))
        }
    }

    // Helper to get just (x,y,z) from raw array
    private fun getVertex3D(index: Int): Vec3 {
        val base = index * 7
        val x = rawVertices[base+0]
        val y = rawVertices[base+1]
        val z = rawVertices[base+2]
        return Vec3(x,y,z)
    }

    // Basic Moller-Trumbore or similar
    private fun intersectRayTriangle(
        origin: Vec3,
        dir: Vec3,
        v0: Vec3,
        v1: Vec3,
        v2: Vec3
    ): Pair<Boolean, Float> {
        // A basic approach.
        // Return (true, t) if intersects, else (false, 0f).
        // For brevity, let's do a placeholder:

        val epsilon = 1e-6f
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        val pvec = dir.cross(edge2)
        val det = edge1.dot(pvec)

        if (abs(det) < epsilon) return Pair(false, 0f)
        val invDet = 1f / det

        val tvec = origin - v0
        val u = tvec.dot(pvec) * invDet
        if (u < 0f || u > 1f) return Pair(false, 0f)

        val qvec = tvec.cross(edge1)
        val v = dir.dot(qvec) * invDet
        if (v < 0f || u + v > 1f) return Pair(false, 0f)

        val t = edge2.dot(qvec) * invDet
        if (t > epsilon) {
            return Pair(true, t)
        }
        return Pair(false, 0f)
    }

    private fun computeNormal(v0: Vec3, v1: Vec3, v2: Vec3): Vec3 {
        val e1 = v1 - v0
        val e2 = v2 - v0
        return e1.cross(e2).normalized()
    }
}

// A minimal Vec3 class
data class Vec3(var x: Float, var y: Float, var z: Float) {
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    fun dot(o: Vec3) = x*o.x + y*o.y + z*o.z
    fun cross(o: Vec3) = Vec3(
        y*o.z - z*o.y,
        z*o.x - x*o.z,
        x*o.y - y*o.x
    )
    fun length() = kotlin.math.sqrt(x*x + y*y + z*z)
    fun normalized(): Vec3 {
        val l = length()
        return if (l < 1e-9) this else Vec3(x/l, y/l, z/l)
    }
}
