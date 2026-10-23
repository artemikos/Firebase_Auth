package com.example.secureauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.secureauth.data.AuthRepository
import com.example.secureauth.ui.AuthScreen
import com.example.secureauth.ui.AuthViewModel
import com.example.secureauth.ui.TasksScreen
import com.example.secureauth.ui.TasksViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    private val authRepository by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    var user by remember {
                        mutableStateOf<FirebaseUser?>(authRepository.currentUser)
                    }
                    val authViewModel = remember { AuthViewModel() }
                    val tasksViewModel = remember { TasksViewModel() }

                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener {
                            user = it.currentUser
                        }
                        FirebaseAuth.getInstance().addAuthStateListener(listener)
                        onDispose {
                            FirebaseAuth.getInstance().removeAuthStateListener(listener)
                        }
                    }

                    val currentUser = user
                    if (currentUser == null) {
                        AuthScreen(activity = this, viewModel = authViewModel)
                    } else {
                        TasksScreen(
                            user = currentUser,
                            activity = this,
                            tasksViewModel = tasksViewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
