// app/src/main/java/com/example/plyviewer/data/AnnotationRepository.kt
package com.example.plyviewer.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationRepository @Inject constructor(private val dao: AnnotationDao) {
    suspend fun insert(annotation: AnnotationEntity) = dao.insert(annotation)
    suspend fun getAll() = dao.getAll()
}
