package com.example.plyviewer.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class My3DAnnotationRepository @Inject constructor(
    private val dao: My3DAnnotationDao
) {
    suspend fun insert(annotation: My3DAnnotationEntity) = dao.insert(annotation)
    suspend fun getAll() = dao.getAll()
    suspend fun deleteAll() = dao.deleteAll()
}
