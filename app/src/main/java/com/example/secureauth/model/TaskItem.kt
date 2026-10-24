package com.example.secureauth.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TaskItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val ownerUid: String = "",
    val createdAt: Timestamp? = null
)