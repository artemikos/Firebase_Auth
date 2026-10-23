package com.example.secureauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen(user: FirebaseUser, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Профиль", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileRow("Имя", user.displayName ?: "Не указано")
                ProfileRow("Email", user.email ?: "Не указан")
                ProfileRow("UID", user.uid)
                ProfileRow("Email подтверждён", if (user.isEmailVerified) "Да" else "Нет")
                ProfileRow(
                    "Провайдеры",
                    user.providerData.map { it.providerId }
                        .filter { it != "firebase" }.distinct().joinToString(", ")
                        .ifBlank { "Не определены" }
                )
            }
        }
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
