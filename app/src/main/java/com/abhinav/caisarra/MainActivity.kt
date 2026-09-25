package com.abhinav.caisarra

import androidx.activity.ComponentActivity
import kotlinx.coroutines.selects.SelectInstance
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        enableEdgeToEdge()
        setContent {
        }



    }
}