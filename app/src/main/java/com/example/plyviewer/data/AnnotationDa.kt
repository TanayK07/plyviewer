package com.example.plyviewer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnnotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations")
    suspend fun getAll(): List<AnnotationEntity>

    @Query("DELETE FROM annotations")
    suspend fun deleteAll()
}
