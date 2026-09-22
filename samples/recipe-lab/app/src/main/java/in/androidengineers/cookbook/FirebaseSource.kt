package `in`.androidengineers.cookbook

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.flow.mapNotNull

/** Firebase configuration and App Check initialization belong to the application, not the prompt. */
class FirebaseSource(modelName: String) : TextSource {
    init { require(modelName.isNotBlank()) { "Enter an enabled Firebase model name" } }
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(modelName)
    override fun stream(prompt: String) = model.generateContentStream(prompt).mapNotNull { it.text }
}
