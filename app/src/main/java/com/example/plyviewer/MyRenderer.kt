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

class MyRenderer(private val context: Context) : GLSurfaceView.Renderer {
    companion object {
        private const val TAG = "MyRenderer"
    }

    // Camera parameters (modifiable via gestures)
    var cameraDistance = 3f
    var cameraTranslateX = 0f
    var cameraTranslateY = 0f
    fun resetCamera() {
        cameraDistance = 3f
        cameraTranslateX = 0f
        cameraTranslateY = 0f
    }

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: IntBuffer
    private var indexCount: Int = 0
    private var vertexCount: Int = 0

    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var program: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Load and parse the PLY file.
        try {
            val inputStream = context.assets.open("model.ply")
            val plyModel: PlyModel = PlyParser().parse(inputStream)
            vertexCount = plyModel.vertices.size / 7
            indexCount = plyModel.indices.size
            Log.d(TAG, "Parsed $vertexCount vertices and ${if(indexCount==0) 0 else indexCount/3} faces")

            vertexBuffer = ByteBuffer.allocateDirect(plyModel.vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(plyModel.vertices)
                    position(0)
                }

            if (indexCount > 0) {
                indexBuffer = ByteBuffer.allocateDirect(plyModel.indices.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer().apply {
                        put(plyModel.indices)
                        position(0)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading or parsing PLY file", e)
        }

        program = createProgram(vertexShaderCode, fragmentShaderCode)
        if (program == 0) {
            Log.e(TAG, "Failed to create shader program")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Update view matrix using camera parameters.
        Matrix.setLookAtM(viewMatrix, 0,
            cameraTranslateX, cameraTranslateY, cameraDistance,
            cameraTranslateX, cameraTranslateY, 0f,
            0f, 1f, 0f)

        // Rotate model for demonstration.
        val angle = (System.currentTimeMillis() % 3600) * 0.1f
        Matrix.setRotateM(modelMatrix, 0, angle, 0f, 1f, 0f)
        // Optionally scale the model (adjust as needed)
        // Matrix.scaleM(modelMatrix, 0, 0.1f, 0.1f, 0.1f)

        // Compute MVP: proj * view * model.
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)

        GLES30.glUseProgram(program)

        // Set up attributes:
        // a_Position: first 3 floats out of 7.
        val posHandle = GLES30.glGetAttribLocation(program, "a_Position")
        GLES30.glEnableVertexAttribArray(posHandle)
        GLES30.glVertexAttribPointer(posHandle, 3, GLES30.GL_FLOAT, false, 7 * 4, vertexBuffer)

        // a_Color: next 4 floats.
        val colorHandle = GLES30.glGetAttribLocation(program, "a_Color")
        vertexBuffer.position(3)
        GLES30.glEnableVertexAttribArray(colorHandle)
        GLES30.glVertexAttribPointer(colorHandle, 4, GLES30.GL_FLOAT, false, 7 * 4, vertexBuffer)
        vertexBuffer.position(0)

        // Pass MVP matrix.
        val mvpHandle = GLES30.glGetUniformLocation(program, "u_MVPMatrix")
        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        // Draw model: if there are faces, use glDrawElements; otherwise, glDrawArrays (as points).
        if (indexCount > 0) {
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_INT, indexBuffer)
        } else {
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, vertexCount)
        }

        GLES30.glDisableVertexAttribArray(posHandle)
        GLES30.glDisableVertexAttribArray(colorHandle)

        Log.d(TAG, "Frame rendered at angle: $angle")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Error compiling shader: ${GLES30.glGetShaderInfoLog(shader)}")
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) return 0
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) return 0

        val prog = GLES30.glCreateProgram()
        GLES30.glAttachShader(prog, vertexShader)
        GLES30.glAttachShader(prog, fragmentShader)
        GLES30.glLinkProgram(prog)
        val linked = IntArray(1)
        GLES30.glGetProgramiv(prog, GLES30.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program: ${GLES30.glGetProgramInfoLog(prog)}")
            GLES30.glDeleteProgram(prog)
            return 0
        }
        return prog
    }

    // Vertex shader: uses position and color; sets point size for points.
    private val vertexShaderCode = """
        #version 300 es
        uniform mat4 u_MVPMatrix;
        in vec3 a_Position;
        in vec4 a_Color;
        out vec4 v_Color;
        void main() {
            gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
            gl_PointSize = 3.0;
            v_Color = a_Color;
        }
    """.trimIndent()

    // Fragment shader: outputs the vertex color.
    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec4 v_Color;
        out vec4 fragColor;
        void main() {
            fragColor = v_Color;
        }
    """.trimIndent()
}
