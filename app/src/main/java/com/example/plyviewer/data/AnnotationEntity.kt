package com.example.plyviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shape: String,
    val label: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)
