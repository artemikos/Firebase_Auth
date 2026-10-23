package com.example.secureauth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureauth.data.TasksRepository
import com.example.secureauth.model.TaskItem
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: TasksRepository = TasksRepository()) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private var currentUid: String? = null
    private var registration: ListenerRegistration? = null

    fun observeUser(uid: String) {
        if (currentUid == uid && registration != null) return
        registration?.remove()
        currentUid = uid
        _tasks.value = emptyList()
        _error.value = null
        registration = repository.listenTasks(
            uid,
            onTasksChanged = { _tasks.value = it },
            onError = { _error.value = it }
        )
    }

    fun addTask(title: String) {
        val uid = currentUid ?: return
        if (title.isBlank() || _busy.value) return
        perform { repository.addTask(uid, title) }
    }

    fun toggleTask(task: TaskItem) {
        val uid = currentUid ?: return
        perform { repository.setCompleted(uid, task.id, !task.completed) }
    }

    fun deleteTask(task: TaskItem) {
        val uid = currentUid ?: return
        perform { repository.deleteTask(uid, task.id) }
    }

    private fun perform(action: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _error.value = null
            try {
                action()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Не удалось выполнить действие."
            } finally {
                _busy.value = false
            }
        }
    }

    override fun onCleared() {
        registration?.remove()
        registration = null
        super.onCleared()
    }
}
