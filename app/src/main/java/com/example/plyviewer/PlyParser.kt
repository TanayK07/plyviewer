package com.example.plyviewer

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class PlyParser {
    data class PlyModel(
        val vertices: FloatArray,  // Each vertex: [x, y, z, r, g, b, a]
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
                    tokens.isNotEmpty() && tokens[0] == "element" && tokens[1] == "vertex" ->
                        vertexCount = tokens[2].toInt()
                    tokens.isNotEmpty() && tokens[0] == "element" && tokens[1] == "face" ->
                        faceCount = tokens[2].toInt()
                    tokens.isNotEmpty() && tokens[0] == "end_header" ->
                        headerEnded = true
                }
            } else {
                if (vertexCount > 0) {
                    if (tokens.size >= 10) {
                        // Position (x,y,z)
                        vertexList.add(tokens[0].toFloat())
                        vertexList.add(tokens[1].toFloat())
                        vertexList.add(tokens[2].toFloat())
                        // Skip normals (tokens[3..5])
                        // Color: tokens[6..9], convert from 0-255 to 0-1.
                        vertexList.add(tokens[6].toFloat() / 255f)
                        vertexList.add(tokens[7].toFloat() / 255f)
                        vertexList.add(tokens[8].toFloat() / 255f)
                        vertexList.add(tokens[9].toFloat() / 255f)
                    }
                    vertexCount--
                } else if (faceCount > 0) {
                    // Expect first token = number of vertices in face (assume 3)
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
        Log.d("PlyParser", "Parsed ${vertexList.size / 7} vertices and ${if(indexList.isEmpty()) 0 else indexList.size / 3} faces")
        return PlyModel(vertices = vertexList.toFloatArray(), indices = indexList.toIntArray())
    }
}
