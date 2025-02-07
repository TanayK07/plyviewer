package com.example.plyviewer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface My3DAnnotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(annotation: My3DAnnotationEntity)

    @Query("SELECT * FROM annotations3d")
    suspend fun getAll(): List<My3DAnnotationEntity>

    @Query("DELETE FROM annotations3d")
    suspend fun deleteAll()
}
