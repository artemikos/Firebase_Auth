package com.example.secureauth.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(activity: Activity, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // только в памяти, никуда не сохраняется
    var isRegister by remember { mutableStateOf(false) }
    val busy = viewModel.busy
    val canSubmit = email.contains("@") && password.length >= 6 && !busy

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(if (isRegister) "Регистрация" else "Вход", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль (минимум 6 символов)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (isRegister) viewModel.register(email, password, activity)
                else viewModel.login(email, password)
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isRegister) "Зарегистрироваться" else "Войти") }

        OutlinedButton(
            onClick = { viewModel.loginWithCredentialManager(activity) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Войти через Google или сохранённый пароль") }

        TextButton(
            onClick = { isRegister = !isRegister },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isRegister) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться") }

        if (busy) CircularProgressIndicator()
        viewModel.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}