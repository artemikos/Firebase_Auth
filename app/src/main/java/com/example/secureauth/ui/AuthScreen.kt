package com.example.secureauth.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AuthScreen(activity: Activity, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    val busy by viewModel.busy.collectAsState()
    val message by viewModel.message.collectAsState()
    val valid = email.contains("@") && password.length >= 6

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isRegister) "Регистрация" else "Вход", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessage() },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessage() },
            label = { Text("Пароль (минимум 6 символов)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (isRegister) viewModel.register(email, password, activity)
                else viewModel.login(email, password)
            },
            enabled = valid && !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isRegister) "Зарегистрироваться" else "Войти") }

        OutlinedButton(
            onClick = { viewModel.loginWithCredentialManager(activity) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Войти через Google или сохранённый пароль") }

        TextButton(
            onClick = { isRegister = !isRegister; viewModel.clearMessage() },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться")
        }
        if (busy) {
            Spacer(Modifier.height(12.dp))
            CircularProgressIndicator()
        }
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            Text(message.orEmpty(), color = MaterialTheme.colorScheme.primary)
        }
    }
}
