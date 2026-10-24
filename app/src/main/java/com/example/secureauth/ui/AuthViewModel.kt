package com.example.secureauth.ui

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureauth.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Текущий пользователь (null = не вошли). Меняется при входе, выходе и отзыве сессии.
    val user: StateFlow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, auth.currentUser)

    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun register(email: String, password: String, activity: Activity) = launchAuth {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        runCatching {
            CredentialManager.create(activity)
                .createCredential(activity, CreatePasswordRequest(email.trim(), password))
        }
    }

    fun login(email: String, password: String) = launchAuth {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun loginWithCredentialManager(activity: Activity) = launchAuth {
        val googleOption = GetGoogleIdOption.Builder()
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetPasswordOption())
            .addCredentialOption(googleOption)
            .build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential

        when {
            credential is PasswordCredential ->
                auth.signInWithEmailAndPassword(credential.id, credential.password).await()

            credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {

                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
            }

            else -> error("Выбран неподдерживаемый тип учётных данных")
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            runCatching { CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest()) }
            auth.signOut()
            message = null
        }
    }

    private fun launchAuth(action: suspend () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            message = null
            try {
                action()
            } catch (_: GetCredentialCancellationException) {
            } catch (e: Exception) {
                message = friendlyError(e)
            } finally {
                busy = false
            }
        }
    }

    private fun friendlyError(e: Exception): String {
        val text = e.message.orEmpty()
        return when {
            text.contains("already in use", true) -> "Этот email уже зарегистрирован."
            text.contains("badly formatted", true) -> "Проверьте формат email."
            text.contains("incorrect", true) || text.contains("invalid", true) -> "Неверный email или пароль."
            text.contains("network", true) -> "Проверьте подключение к интернету."
            else -> e.localizedMessage ?: "Произошла ошибка."
        }
    }
}