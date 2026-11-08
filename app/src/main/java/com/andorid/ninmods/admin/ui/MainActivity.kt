package com.andorid.ninmods.admin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.andorid.ninmods.admin.ui.compose.MainAppScreen
import com.andorid.ninmods.admin.ui.screens.login.AdminLoginScreen
import com.andorid.ninmods.admin.ui.theme.APatchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            APatchTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                if (!isLoggedIn) {
                    val isLoading by viewModel.isLoading.collectAsState()
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    AdminLoginScreen(
                        onLoginClick = { u, p -> viewModel.login(u, p) },
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                } else {
                    MainAppScreen(
                        viewModel = viewModel,
                        onSignOut = { viewModel.logout() }
                    )
                }
            }
        }
    }
}
