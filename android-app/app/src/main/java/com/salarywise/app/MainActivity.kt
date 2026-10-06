package com.salarywise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.salarywise.app.ui.navigation.SalaryWiseNavGraph
import com.salarywise.app.ui.theme.SalaryWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SalaryWiseApplication
        val repository = app.repository

        setContent {
            val currentUser by repository.getCurrentUserFlow().collectAsState(initial = null)
            val isDarkTheme = currentUser?.isDarkMode ?: isSystemInDarkTheme()

            SalaryWiseTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
