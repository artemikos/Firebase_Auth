package com.example.secureauth.data

import com.example.secureauth.model.TaskItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TasksRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private fun collection(uid: String) =
        db.collection("users").document(uid).collection("tasks")

    fun listenTasks(
        uid: String,
        onTasksChanged: (List<TaskItem>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration = collection(uid)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error.localizedMessage ?: "Не удалось загрузить задачи")
                return@addSnapshotListener
            }
            onTasksChanged(snapshot?.documents.orEmpty().map { doc ->
                TaskItem(
                    id = doc.id,
                    title = doc.getString("title").orEmpty(),
                    completed = doc.getBoolean("completed") ?: false,
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                )
            })
        }

    suspend fun addTask(uid: String, title: String) {
        collection(uid).add(
            mapOf(
                "title" to title.trim(),
                "completed" to false,
                "ownerUid" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun setCompleted(uid: String, taskId: String, completed: Boolean) {
        collection(uid).document(taskId).update("completed", completed).await()
    }

    suspend fun deleteTask(uid: String, taskId: String) {
        collection(uid).document(taskId).delete().await()
    }
}
