package com.example.secureauth.ui

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureauth.data.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun register(email: String, password: String, activity: Activity) {
        runAuth {
            repository.register(email, password)
            try {
                CredentialManager.create(activity).createCredential(
                    context = activity,
                    request = CreatePasswordRequest(email.trim(), password)
                )
                _message.value = "Регистрация успешна. Пароль сохранён."
            } catch (_: Exception) {
                _message.value = "Регистрация успешна."
            }
        }
    }

    fun login(email: String, password: String) {
        runAuth {
            repository.login(email, password)
            _message.value = null
        }
    }

    fun loginWithCredentialManager(activity: Activity) {
        runAuth {
            val manager = CredentialManager.create(activity)
            val googleOption = GetGoogleIdOption.Builder()
                .setServerClientId(activity.getString(com.example.secureauth.R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(GetPasswordOption())
                .addCredentialOption(googleOption)
                .build()
            val result = manager.getCredential(activity, request)

            when (val credential = result.credential) {
                is PasswordCredential -> repository.login(credential.id, credential.password)
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        repository.loginWithGoogle(googleCredential.idToken)
                    } else error("Выбран неподдерживаемый тип учётных данных")
                }
                else -> error("Не удалось определить учётные данные")
            }
            _message.value = null
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            try {
                CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
                // Выход из Firebase выполняется и при ошибке очистки Credential Manager.
            }
            repository.signOut()
        }
    }

    private fun runAuth(action: suspend () -> Unit) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = null
            try {
                action()
            } catch (error: Exception) {
                _message.value = friendlyError(error)
            } finally {
                _busy.value = false
            }
        }
    }

    private fun friendlyError(error: Throwable): String {
        val text = error.message.orEmpty()
        return when {
            text.contains("already in use", true) -> "Этот email уже зарегистрирован."
            text.contains("badly formatted", true) -> "Проверьте формат email."
            text.contains("password is invalid", true) ||
                text.contains("invalid credential", true) -> "Неверный email или пароль."
            text.contains("network", true) -> "Проверьте подключение к интернету."
            else -> error.localizedMessage ?: "Произошла ошибка."
        }
    }
}
