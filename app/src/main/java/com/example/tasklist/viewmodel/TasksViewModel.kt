package com.example.tasklist.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.tasklist.data.PreferencesManager
import com.example.tasklist.data.SortOrder
import com.example.tasklist.db.TaskDao
import com.example.tasklist.model.Task
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData("search_query", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    // combine the search query and filter preferences and create a Pair, then call the getTasks method in the taskDao and use the values
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    // call the method on preferences manager to update the sortOrder value in the dataStore
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    // call the method on preferences manager to update the hideCompleted value in the dataStore
    fun onHideCompletedClicked(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onAddNewTaskClicked() =
        viewModelScope.launch { tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen) }

    fun onEditTaskClick(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onUndoDeleteClick(task: Task) =
        viewModelScope.launch {
            taskDao.insert(task)
        }

    fun onTaskCheckboxClick(task: Task, checked: Boolean) = viewModelScope.launch {
        val task = task.copy(isCompleted = checked)
        taskDao.update(task)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(
            TasksEvent.NavigateToDeleteAllTasksScreen
        )
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowTaskSavedUpdatedConfirmationMessage(val msg: String) : TasksEvent()
        object NavigateToDeleteAllTasksScreen : TasksEvent()
    }
}