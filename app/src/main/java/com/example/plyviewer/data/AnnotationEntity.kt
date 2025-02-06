package com.example.plyviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,       // e.g. "spray", "sand", "obstacle"
    val x: Float,           // x coordinate (in overlay view pixels)
    val y: Float,           // y coordinate
    val width: Float,
    val height: Float
)
