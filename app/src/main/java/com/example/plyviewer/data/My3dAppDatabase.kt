@Database(
    entities = [
        AnnotationEntity::class,  // your 2D entity
        My3DAnnotationEntity::class  // new 3D entity
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun annotationDao(): AnnotationDao
    abstract fun my3DAnnotationDao(): My3DAnnotationDao
    // ...
}
