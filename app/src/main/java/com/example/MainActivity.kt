package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.theme.AIVoiceStudioTheme
import com.example.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            AIVoiceStudioTheme {
                MainScreen(viewModel)
            }
        }
    }
}
