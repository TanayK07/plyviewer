package com.example.plyviewer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnnotationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun annotationDao(): AnnotationDao
}
