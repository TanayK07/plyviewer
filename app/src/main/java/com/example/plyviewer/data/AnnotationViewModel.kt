// app/src/main/java/com/example/plyviewer/AnnotationViewModel.kt
package com.example.plyviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plyviewer.data.AnnotationEntity
import com.example.plyviewer.data.AnnotationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnotationViewModel @Inject constructor(
    private val repository: AnnotationRepository
) : ViewModel() {
    private val _annotations = MutableStateFlow<List<AnnotationEntity>>(emptyList())
    val annotations = _annotations.asStateFlow()

    init {
        loadAnnotations()
    }

    fun loadAnnotations() {
        viewModelScope.launch {
            _annotations.value = repository.getAll()
        }
    }

    fun addAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch {
            repository.insert(annotation)
            loadAnnotations()
        }
    }
}
