package com.example.tasklist.db

import androidx.room.*
import com.example.tasklist.data.SortOrder
import com.example.tasklist.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.SORT_BY_DATE -> getTasksSortedByDate(query, hideCompleted)
            SortOrder.SORT_BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    // get data from db sorted by name
    @Query("SELECT * FROM task_table WHERE (isCompleted != :hideCompleted or isCompleted = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name ")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    // get data from db sorted by date
    @Query("SELECT * FROM task_table WHERE (isCompleted != :hideCompleted or isCompleted = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDate(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    // get all tasks that are completed Sorted By Name
    @Query("SELECT * FROM task_table")
    fun getTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    // delete all completed tasks
    @Query("DELETE FROM task_table WHERE isCompleted=1")
    suspend fun deleteCompletedTasks()
}