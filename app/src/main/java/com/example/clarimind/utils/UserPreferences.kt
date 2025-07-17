package com.example.clarimind.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to manage user preferences and app state
 */
class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if the user has completed the assessment test
     */
    fun hasCompletedTest(): Boolean {
        return prefs.getBoolean(KEY_TEST_COMPLETED, false)
    }
    
    /**
     * Mark the assessment test as completed
     */
    fun setTestCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_TEST_COMPLETED, completed).apply()
    }
    
    /**
     * Get the user's last detected mood
     */
    fun getLastMood(): String? {
        return prefs.getString(KEY_LAST_MOOD, null)
    }
    
    /**
     * Save the user's last detected mood
     */
    fun setLastMood(mood: String) {
        prefs.edit().putString(KEY_LAST_MOOD, mood).apply()
    }
    
    /**
     * Get the user's last PHI scores
     */
    fun getLastPHIScores(): Triple<Double, Double, Double>? {
        val rememberedWellBeing = prefs.getFloat(KEY_REMEMBERED_WELLBEING, -1f).toDouble()
        val experiencedWellBeing = prefs.getFloat(KEY_EXPERIENCED_WELLBEING, -1f).toDouble()
        val combinedPHI = prefs.getFloat(KEY_COMBINED_PHI, -1f).toDouble()
        
        return if (rememberedWellBeing >= 0 && experiencedWellBeing >= 0 && combinedPHI >= 0) {
            Triple(rememberedWellBeing, experiencedWellBeing, combinedPHI)
        } else {
            null
        }
    }
    
    /**
     * Save the user's PHI scores
     */
    fun savePHIScores(rememberedWellBeing: Double, experiencedWellBeing: Double, combinedPHI: Double) {
        prefs.edit()
            .putFloat(KEY_REMEMBERED_WELLBEING, rememberedWellBeing.toFloat())
            .putFloat(KEY_EXPERIENCED_WELLBEING, experiencedWellBeing.toFloat())
            .putFloat(KEY_COMBINED_PHI, combinedPHI.toFloat())
            .apply()
    }
    
    /**
     * Clear all user preferences
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    companion object {
        private const val PREF_NAME = "clarimind_prefs"
        private const val KEY_TEST_COMPLETED = "test_completed"
        private const val KEY_LAST_MOOD = "last_mood"
        private const val KEY_REMEMBERED_WELLBEING = "remembered_wellbeing"
        private const val KEY_EXPERIENCED_WELLBEING = "experienced_wellbeing"
        private const val KEY_COMBINED_PHI = "combined_phi"
    }
}