package `in`.androidengineers.pocketcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import `in`.androidengineers.pocketcards.data.*
import `in`.androidengineers.pocketcards.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = FileDeckRepository(java.io.File(filesDir, "decks.json"))
        val generator = FirebaseCardGenerator(FirebaseApp.getApps(this).isNotEmpty())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PocketCardsViewModel(repository, generator) as T
        }
        setContent { PocketTheme { PocketCardsApp(viewModel(factory = factory)) } }
    }
}
