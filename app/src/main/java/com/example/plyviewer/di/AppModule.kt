package com.example.plyviewer.di

import android.content.Context
import androidx.room.Room
import com.example.plyviewer.data.AppDatabase
import com.example.plyviewer.data.AnnotationDao
import com.example.plyviewer.data.AnnotationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "annotations.db").build()

    @Provides
    fun provideAnnotationDao(database: AppDatabase): AnnotationDao =
        database.annotationDao()

    @Provides
    @Singleton
    fun provideAnnotationRepository(dao: AnnotationDao): AnnotationRepository =
        AnnotationRepository(dao)
}
