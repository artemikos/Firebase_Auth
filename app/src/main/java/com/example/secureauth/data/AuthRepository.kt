package com.example.secureauth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String): FirebaseUser =
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .await().user ?: error("Не удалось создать пользователя")

    suspend fun login(email: String, password: String): FirebaseUser =
        auth.signInWithEmailAndPassword(email.trim(), password)
            .await().user ?: error("Не удалось выполнить вход")

    suspend fun loginWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await().user
            ?: error("Не удалось выполнить вход через Google")
    }

    fun signOut() = auth.signOut()
}
