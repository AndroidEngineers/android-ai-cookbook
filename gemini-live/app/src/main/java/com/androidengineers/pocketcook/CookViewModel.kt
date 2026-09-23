package com.androidengineers.pocketcook
import androidx.lifecycle.ViewModel
import com.androidengineers.pocketcook.data.*
import kotlinx.coroutines.flow.*

enum class Screen { Library, Details, Cooking, Finished }
data class CookState(val screen: Screen = Screen.Library, val recipeId: String = "pasta", val step: Int = 0, val checked: Set<Int> = emptySet(), val notice: String? = null) {
 val recipe get() = Recipes.get(recipeId)
}
class CookViewModel(private val progress: ProgressStore, val live: LiveConnection) : ViewModel() {
 private val mutable = MutableStateFlow(CookState())
 val state = mutable.asStateFlow()
 private var key = ""
 var model: String = "gemini-3.8-live"; private set
 val configured get() = key.isNotBlank()
 fun configure(key: String, model: String) { if (BuildConfig.DEBUG) { this.key = key.trim(); this.model = model.trim() } }
 fun select(id: String) {
  live.end("Ready when you are"); val recipe = Recipes.get(id)
  mutable.value = CookState(Screen.Details, recipe.id, boundedStep(progress.read(recipe.id), recipe.steps.size))
 }
 fun cook() { mutable.update { it.copy(screen = Screen.Cooking, notice = null) } }
 fun check(index: Int) { mutable.update { it.copy(checked = if (index in it.checked) it.checked - index else it.checked + index) } }
 fun step(delta: Int) {
  val s = state.value; val next = boundedStep(s.step + delta, s.recipe.steps.size)
  progress.write(s.recipeId, next)
  mutable.update { it.copy(step = next) }; live.updateStep(next)
 }
 fun finish() { live.end(); progress.write(state.value.recipeId, 0); mutable.update { it.copy(screen = Screen.Finished) } }
 fun back() {
  live.end()
  mutable.update { it.copy(screen = if (it.screen == Screen.Cooking) Screen.Details else Screen.Library, notice = null) }
 }
 fun startVoice() {
  if (!BuildConfig.DEBUG) { notice("Voice deployment setup is coming in a later chapter. Recipes work offline."); return }
  if (!configured) { notice("Add your own Gemini API key in connection settings first."); return }
  mutable.update { it.copy(notice = null) }
  live.start(key, model, state.value.recipe, state.value.step)
 }
 fun notice(text: String?) { mutable.update { it.copy(notice = text) } }
 fun background() { live.end("Voice stopped while the app was away. Tap to start again."); key = "" }
 override fun onCleared() { live.end(); if (live is GeminiLiveConnection) live.dispose(); key = "" }
}
