package com.androidengineers.pocketchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.androidengineers.pocketchat.ui.PocketChatApp

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val model = ViewModelProvider(this, viewModelFactory { initializer { ChatViewModel((application as PocketChatApplication).repository, createSavedStateHandle()) } })[ChatViewModel::class.java]
        setContent { PocketChatApp(model) }
    }
}
