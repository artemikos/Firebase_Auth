package com.example.secureauth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.secureauth.model.TaskItem
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TasksViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var error by mutableStateOf<String?>(null)
        private set

    private fun tasks(uid: String) =
        db.collection("users").document(uid).collection("tasks")

    fun observe(uid: String): Flow<List<TaskItem>> = callbackFlow {
        error = null
        val registration = tasks(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.localizedMessage
                } else if (snapshot != null) {
                    trySend(snapshot.toObjects(TaskItem::class.java))
                }
            }
        awaitClose { registration.remove() }
    }

    fun add(uid: String, title: String) {
        if (title.isBlank()) return
        tasks(uid).add(
            mapOf(
                "title" to title.trim(),
                "completed" to false,
                "ownerUid" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).report()
    }

    fun toggle(uid: String, task: TaskItem) {
        tasks(uid).document(task.id).update("completed", !task.completed).report()
    }

    fun delete(uid: String, task: TaskItem) {
        tasks(uid).document(task.id).delete().report()
    }

    private fun Task<*>.report() {
        error = null
        addOnFailureListener { error = it.localizedMessage }
    }
}