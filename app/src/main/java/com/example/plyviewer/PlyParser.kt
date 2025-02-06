package com.example.plyviewer

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class PlyParser {
    data class PlyModel(
        val vertices: FloatArray,
        val indices: IntArray
    )

    fun parse(inputStream: InputStream): PlyModel {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var vertexCount = 0
        var faceCount = 0
        var headerEnded = false
        val vertexList = mutableListOf<Float>()
        val indexList = mutableListOf<Int>()

        reader.forEachLine { line ->
            val tokens = line.trim().split("\\s+".toRegex())
            if (!headerEnded) {
                when {
                    tokens.isNotEmpty() && tokens[0] == "element" && tokens[1] == "vertex" -> {
                        vertexCount = tokens[2].toInt()
                    }
                    tokens.isNotEmpty() && tokens[0] == "element" && tokens[1] == "face" -> {
                        faceCount = tokens[2].toInt()
                    }
                    tokens.isNotEmpty() && tokens[0] == "end_header" -> {
                        headerEnded = true
                    }
                }
            } else {
                if (vertexCount > 0) {
                    // Expect at least 3 floats (x, y, z)
                    if (tokens.size >= 3) {
                        vertexList.add(tokens[0].toFloat())
                        vertexList.add(tokens[1].toFloat())
                        vertexList.add(tokens[2].toFloat())
                    }
                    vertexCount--
                } else if (faceCount > 0) {
                    // Expect the first token is the number of vertices (assume 3 for triangles).
                    val count = tokens[0].toInt()
                    if (count == 3 && tokens.size >= 4) {
                        indexList.add(tokens[1].toInt())
                        indexList.add(tokens[2].toInt())
                        indexList.add(tokens[3].toInt())
                    }
                    faceCount--
                }
            }
        }
        Log.d("PlyParser", "Parsed ${vertexList.size / 3} vertices and ${indexList.size / 3} faces")
        return PlyModel(vertices = vertexList.toFloatArray(), indices = indexList.toIntArray())
    }
}
