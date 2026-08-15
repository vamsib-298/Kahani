package com.vl.kahani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vl.kahani.ui.KahaniApp
import com.vl.kahani.ui.theme.KahaniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KahaniTheme {
                KahaniApp()
            }
        }
    }
}
