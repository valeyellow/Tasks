package com.example.tasklist.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tasklist.di.ApplicationScope
import com.example.tasklist.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,  // Provider is used to get an instance LAZILY (it breaks the circular dependency between taskDao, database and the callback class
        @ApplicationScope
        private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get()
                .taskDao()   // the database variable gets instantiated when we call the .get() method on it

            applicationScope.launch {
                dao.insert(Task(name = "Get fruits", important = true, isCompleted = false))
                dao.insert(Task(name = "Get eggs"))
                dao.insert(Task(name = "Do homework"))
                dao.insert(Task(name = "Read books"))
                dao.insert(Task(name = "Do laundry", important = false, isCompleted = false))
                dao.insert(Task(name = "Practice Android", important = true, isCompleted = true))
            }
        }
    }
}