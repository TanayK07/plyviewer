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

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: IntBuffer
    private var indexCount: Int = 0
    private var vertexCount: Int = 0

    // Transformation matrices.
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var program: Int = 0

    // Light direction.
    private val lightDirection = floatArrayOf(0.0f, 0.0f, 1.0f)

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES30.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // (Removed glEnable(GL_PROGRAM_POINT_SIZE) and glPointSize calls.)

        try {
            val inputStream = context.assets.open("model.ply")
            val plyModel: PlyModel = PlyParser().parse(inputStream)
            vertexCount = plyModel.vertices.size / 3
            indexCount = plyModel.indices.size
            Log.d(TAG, "Parsed $vertexCount vertices and ${indexCount / 3} faces")

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

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 3f,
            0f, 0f, 0f,
            0f, 1f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val angle = (System.currentTimeMillis() % 3600) * 0.1f
        Matrix.setRotateM(modelMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)

        GLES30.glUseProgram(program)

        val posHandle = GLES30.glGetAttribLocation(program, "a_Position")
        GLES30.glEnableVertexAttribArray(posHandle)
        GLES30.glVertexAttribPointer(posHandle, 3, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer)

        val mvpHandle = GLES30.glGetUniformLocation(program, "u_MVPMatrix")
        GLES30.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        val lightHandle = GLES30.glGetUniformLocation(program, "u_LightDirection")
        GLES30.glUniform3fv(lightHandle, 1, lightDirection, 0)

        if (indexCount > 0) {
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_INT, indexBuffer)
        } else {
            GLES30.glDrawArrays(GLES30.GL_POINTS, 0, vertexCount)
        }

        GLES30.glDisableVertexAttribArray(posHandle)
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

    // Vertex shader with a fixed point size.
    private val vertexShaderCode = """
        #version 300 es
        uniform mat4 u_MVPMatrix;
        in vec3 a_Position;
        void main() {
            gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
            gl_PointSize = 2.0;  // Set the size of points.
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        uniform vec3 u_LightDirection;
        out vec4 fragColor;
        void main() {
            float ambient = 0.3;
            float diffuse = max(dot(normalize(vec3(0.0, 0.0, 1.0)), normalize(u_LightDirection)), 0.0);
            vec3 baseColor = vec3(0.6, 0.8, 1.0);
            vec3 color = baseColor * (ambient + diffuse);
            fragColor = vec4(color, 1.0);
        }
    """.trimIndent()
}
