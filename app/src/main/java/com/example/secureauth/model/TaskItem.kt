package com.example.secureauth.model

data class TaskItem(
    val id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val ownerUid: String = "",
    val createdAt: Long = 0L
)
