package com.example.plyviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations3d")
data class My3DAnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,     // e.g. "Wall"
    val x: Float,          // 3D coords in model space
    val y: Float,
    val z: Float
)
