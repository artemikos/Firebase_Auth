package com.example.secureauth.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun TasksScreen(
    user: FirebaseUser,
    activity: Activity,
    tasksViewModel: TasksViewModel,
    authViewModel: AuthViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("✓") },
                    label = { Text("Задачи") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("●") },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (selectedTab == 0) {
                TasksContent(user, tasksViewModel)
            } else {
                ProfileScreen(user) { authViewModel.signOut(activity) }
            }
        }
    }
}

@Composable
private fun TasksContent(user: FirebaseUser, viewModel: TasksViewModel) {
    val tasks by remember(user.uid) { viewModel.observe(user.uid) }.collectAsState(emptyList())
    var title by remember { mutableStateOf("") }

    Text("Мои задачи", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Новая задача") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = { viewModel.add(user.uid, title); title = "" },
            enabled = title.isNotBlank()
        ) { Text("Добавить") }
    }

    viewModel.error?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, color = MaterialTheme.colorScheme.error)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { viewModel.toggle(user.uid, task) }
                )
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { viewModel.delete(user.uid, task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
                }
            }
        }
    }
}