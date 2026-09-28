package com.abhinav.caisarra

import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.navigation.AppNavigation


class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        enableEdgeToEdge()
        setContent {
            val authRepository = remember { AuthRepository(applicationContext) }
            AppNavigation(authRepository)
        }

    }
}