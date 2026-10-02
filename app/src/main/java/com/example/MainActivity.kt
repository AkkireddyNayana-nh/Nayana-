package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskBuddyScreen
import com.example.ui.TaskBuddyViewModel
import com.example.ui.TaskBuddyViewModelFactory
import com.example.ui.theme.TaskBuddyTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = TaskRepository(database.taskDao())
    val viewModelFactory = TaskBuddyViewModelFactory(repository)

    setContent {
      val viewModel: TaskBuddyViewModel = viewModel(factory = viewModelFactory)
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      TaskBuddyTheme(themeMode = uiState.themeMode) {
        TaskBuddyScreen(viewModel = viewModel)
      }
    }
  }
}
