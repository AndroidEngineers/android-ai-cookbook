package com.androidengineers.pocketcommunity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.material3.a2ui.catalog.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.androidengineers.pocketcommunity.data.HttpCommunityRepository
import com.androidengineers.pocketcommunity.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                androidx.activity.SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
            navigationBarStyle =
                androidx.activity.SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )
        setContent {
            MaterialTheme(
                colorScheme =
                    lightColorScheme(
                        primary = Color(0xFF6336E8),
                        onPrimary = Color.White,
                        background = Color(0xFFFAF8FF),
                        surface = Color.White,
                        onSurface = Color(0xFF191A38),
                        surfaceVariant = Color(0xFFEFE9FF),
                    )
            ) {
                val catalog = remember { communityCatalog() }
                val vm: CommunityViewModel =
                    viewModel(
                        factory =
                            object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                                    CommunityViewModel(
                                        HttpCommunityRepository(),
                                        catalog,
                                        getSharedPreferences("agent", MODE_PRIVATE)
                                            .getString("endpoint", "")
                                            .orEmpty(),
                                        { endpoint ->
                                            getSharedPreferences("agent", MODE_PRIVATE)
                                                .edit()
                                                .putString("endpoint", endpoint)
                                                .apply()
                                        },
                                    )
                                        as T
                            }
                    )
                CommunityScreen(vm) { url ->
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                }
            }
        }
    }
}
