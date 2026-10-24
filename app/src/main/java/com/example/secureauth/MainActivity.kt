package com.example.secureauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.secureauth.ui.AuthScreen
import com.example.secureauth.ui.AuthViewModel
import com.example.secureauth.ui.TasksScreen
import com.example.secureauth.ui.TasksViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {

                    val user by authViewModel.user.collectAsState()
                    val currentUser = user
                    if (currentUser == null) {
                        AuthScreen(activity = this, viewModel = authViewModel)
                    } else {
                        TasksScreen(currentUser, this, tasksViewModel, authViewModel)
                    }
                }
            }
        }
    }
}