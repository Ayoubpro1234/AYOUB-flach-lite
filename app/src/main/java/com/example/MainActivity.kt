package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.FlashlightScreen
import com.example.ui.FlashlightViewModel
import com.example.ui.theme.FlashlightProTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FlashlightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Observe torch state to keep screen on while light is active
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isTorchOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }

        setContent {
            FlashlightProTheme {
                FlashlightScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Automatically switch off the flashlight when the app leaves the foreground
        if (viewModel.uiState.value.isTorchOn) {
            viewModel.turnOff()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.flashlightController.release()
    }
}
