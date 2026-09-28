package com.abhinav.caisarra

import androidx.activity.ComponentActivity
import kotlinx.coroutines.selects.SelectInstance
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abhinav.caisarra.presentation.navigation.AppNavigation
import com.abhinav.caisarra.presentation.screens.LoginScreen
import com.abhinav.caisarra.presentation.screens.SignUpScreen

//import com.caisaara.ui.theme.CaisaaraTheme

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }

    }
}