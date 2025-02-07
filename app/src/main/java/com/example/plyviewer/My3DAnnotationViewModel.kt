package com.example.plyviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.plyviewer.data.My3DAnnotationEntity
import com.example.plyviewer.data.My3DAnnotationRepository

@HiltViewModel
class My3DAnnotationViewModel @Inject constructor(
    private val repository: My3DAnnotationRepository
) : ViewModel() {

    private val _annotations3d = MutableStateFlow<List<My3DAnnotationEntity>>(emptyList())
    val annotations3d = _annotations3d.asStateFlow()

    init {
        loadAnnotations()
    }

    fun loadAnnotations() {
        viewModelScope.launch {
            _annotations3d.value = repository.getAll()
        }
    }

    fun addAnnotation3D(ann: My3DAnnotationEntity) {
        viewModelScope.launch {
            repository.insert(ann)
            loadAnnotations()
        }
    }

    fun clearAll3D() {
        viewModelScope.launch {
            repository.deleteAll()
            _annotations3d.value = emptyList()
        }
    }
}
