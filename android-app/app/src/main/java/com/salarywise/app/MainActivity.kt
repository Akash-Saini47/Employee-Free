package com.salarywise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.salarywise.app.ui.navigation.SalaryWiseNavGraph
import com.salarywise.app.ui.theme.SalaryWiseTheme
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

private sealed interface UserLoadState {
    data object Loading : UserLoadState
    data class Loaded(val user: com.salarywise.app.data.local.entity.UserEntity?) : UserLoadState
    data class Error(val message: String) : UserLoadState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SalaryWiseApplication
        val repository = app.repository

        setContent {
            var userState by remember { mutableStateOf<UserLoadState>(UserLoadState.Loading) }

            LaunchedEffect(repository) {
                repository.getCurrentUserFlow()
                    .catch { error ->
                        userState = UserLoadState.Error(
                            error.message ?: "Unable to open the local financial database."
                        )
                    }
                    .collect { user ->
                        userState = UserLoadState.Loaded(user)
                    }
            }

            val currentUser =
                (userState as? UserLoadState.Loaded)?.user
            val isDarkTheme =
                currentUser?.isDarkMode ?: isSystemInDarkTheme()

            SalaryWiseTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = userState) {
                        UserLoadState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Loading SalaryWise...",
                                    modifier = Modifier
                                )
                            }
                        }

                        is UserLoadState.Error -> {
                            DatabaseErrorScreen(
                                message = state.message,
                                onRetry = { recreate() }
                            )
                        }

                        is UserLoadState.Loaded -> {
                            val navController = rememberNavController()
                            SalaryWiseNavGraph(
                                navController = navController,
                                repository = repository,
                                currentUser = currentUser
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SalaryWise couldn't open your local data.",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
