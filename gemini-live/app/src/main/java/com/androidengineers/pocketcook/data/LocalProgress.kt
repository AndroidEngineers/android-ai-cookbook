package com.androidengineers.pocketcook.data
import android.content.Context
import androidx.core.content.edit
class LocalProgress(context: Context) : ProgressStore {
 private val preferences = context.getSharedPreferences("recipe_progress", Context.MODE_PRIVATE)
 override fun read(recipe: String) = preferences.getInt(recipe, 0)
 override fun write(recipe: String, step: Int) { preferences.edit { putInt(recipe, step) } }
}
