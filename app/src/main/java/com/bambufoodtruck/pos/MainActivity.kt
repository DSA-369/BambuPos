package com.bambufoodtruck.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bambufoodtruck.pos.core.BambuApplication
import com.bambufoodtruck.pos.presentation.pos.PosScreen
import com.bambufoodtruck.pos.presentation.pos.PosViewModel
import com.bambufoodtruck.pos.ui.theme.BambuPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BambuApplication
        val viewModel = PosViewModel(app.productRepository)

        setContent {
            BambuPosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PosScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}