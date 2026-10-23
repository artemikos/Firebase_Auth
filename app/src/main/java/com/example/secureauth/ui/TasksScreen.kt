package com.example.secureauth.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secureauth.model.TaskItem
import com.google.firebase.auth.FirebaseUser

@Composable
fun TasksScreen(
    user: FirebaseUser,
    activity: Activity,
    tasksViewModel: TasksViewModel,
    authViewModel: AuthViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(user.uid) { tasksViewModel.observeUser(user.uid) }

    val tasks by tasksViewModel.tasks.collectAsState()
    val error by tasksViewModel.error.collectAsState()
    val busy by tasksViewModel.busy.collectAsState()

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
                TasksContent(
                    tasks, error, busy,
                    onAdd = tasksViewModel::addTask,
                    onToggle = tasksViewModel::toggleTask,
                    onDelete = tasksViewModel::deleteTask
                )
            } else {
                ProfileScreen(user) { authViewModel.signOut(activity) }
            }
        }
    }
}

@Composable
private fun TasksContent(
    tasks: List<TaskItem>,
    error: String?,
    busy: Boolean,
    onAdd: (String) -> Unit,
    onToggle: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit
) {
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
            onClick = { onAdd(title); title = "" },
            enabled = title.isNotBlank() && !busy
        ) { Text("Добавить") }
    }

    if (error != null) {
        Spacer(Modifier.height(8.dp))
        Text(error, color = MaterialTheme.colorScheme.error)
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
                    onCheckedChange = { onToggle(task) }
                )
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onDelete(task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
                }
            }
        }
    }
}
