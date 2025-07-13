package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clarimind.config.GeminiConfig
import com.example.clarimind.presentation.model.PHIScore
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatbotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private var userContext: String = ""
    private var generativeModel: GenerativeModel? = null

    fun initializeChat(mood: String, phiScore: PHIScore) {
        userContext = buildUserContext(mood, phiScore)
        
        // Initialize Gemini model
        try {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = GeminiConfig.API_KEY
            )
        } catch (e: Exception) {
            // Fallback to mock responses if API key is not configured
            generativeModel = null
        }
        
        val welcomeMessage = ChatMessage(
            text = buildWelcomeMessage(mood, phiScore),
            isUser = false
        )
        
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMessage)
        )
    }

    fun sendMessage(message: String) {
        if (message.trim().isEmpty()) return

        val userMessage = ChatMessage(
            text = message,
            isUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val response = generateGeminiResponse(message)
                val botMessage = ChatMessage(
                    text = response,
                    isUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + botMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get response: ${e.message}"
                )
            }
        }
    }

    private fun buildUserContext(mood: String, phiScore: PHIScore): String {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        return """
            You are ClariMind, a compassionate AI mental health assistant. Today is $today.
            
            User's Current State:
            - Detected Mood: $mood
            - Happiness Score: ${String.format("%.1f", phiScore.combinedPHI)}/10
            - Remembered Well-being: ${String.format("%.1f", phiScore.rememberedWellBeing)}/10
            - Experienced Well-being: ${String.format("%.1f", phiScore.experiencedWellBeing)}/10
            
            Your Role:
            - Provide empathetic, supportive responses
            - Offer practical mental health advice
            - Suggest activities based on their mood and scores
            - Help them understand their emotional state
            - Provide coping strategies when needed
            - Always prioritize their safety and well-being
            
            Guidelines:
            - Keep responses conversational and warm
            - Be encouraging but realistic
            - Suggest evidence-based mental health practices
            - If they mention severe distress, encourage professional help
            - Focus on actionable, positive steps
            - Respect their privacy and boundaries
        """.trimIndent()
    }

    private fun buildWelcomeMessage(mood: String, phiScore: PHIScore): String {
        val moodEmoji = when (mood.lowercase()) {
            "happy", "joy", "excited" -> "😊"
            "sad", "depressed", "disappointed" -> "😔"
            "angry", "frustrated", "annoyed" -> "😤"
            "calm", "peaceful", "relaxed" -> "😌"
            "anxious", "worried", "stressed" -> "😰"
            "surprised", "shocked" -> "😲"
            else -> "🤔"
        }

        val scoreLevel = when {
            phiScore.combinedPHI >= 8.0 -> "excellent"
            phiScore.combinedPHI >= 6.0 -> "good"
            phiScore.combinedPHI >= 4.0 -> "moderate"
            else -> "challenging"
        }

        return """
            $moodEmoji Hello! I'm ClariMind, your AI mental health companion.
            
            I can see you're feeling ${mood.lowercase()} today, and your overall happiness score is ${String.format("%.1f", phiScore.combinedPHI)}/10, which indicates a $scoreLevel emotional state.
            
            I'm here to support you, offer guidance, and help you navigate your emotional well-being. You can ask me anything - whether you want to understand your mood better, get suggestions for activities, learn coping strategies, or just need someone to talk to.
            
            What would you like to explore today?
        """.trimIndent()
    }

    private suspend fun generateGeminiResponse(userMessage: String): String {
        return try {
            if (generativeModel != null) {
                // Use actual Gemini API
                val prompt = """
                    $userContext
                    
                    User's message: $userMessage
                    
                    Please provide a helpful, empathetic response that considers their current emotional state and happiness scores. Keep your response under 200 words and make it conversational.
                """.trimIndent()

                val response = generativeModel!!.generateContent(prompt)
                response.text ?: generateMockResponse(userMessage.lowercase(), userContext)
            } else {
                // Fallback to mock response
                kotlinx.coroutines.delay(1000) // Simulate API call delay
                generateMockResponse(userMessage.lowercase(), userContext)
            }
        } catch (e: Exception) {
            // Fallback to mock response on error
            generateMockResponse(userMessage.lowercase(), userContext)
        }
    }

    private fun generateMockResponse(userMessage: String, context: String): String {
        return when {
            userMessage.contains("help") || userMessage.contains("support") -> {
                "I'm here to support you! Based on your current state, I'd recommend starting with some gentle self-care activities. Would you like to explore breathing exercises, mindfulness techniques, or perhaps some mood-lifting activities?"
            }
            userMessage.contains("sad") || userMessage.contains("depressed") || userMessage.contains("down") -> {
                "I understand you're feeling down right now, and that's completely valid. Remember that these feelings are temporary. Let's work together to find some small steps that might help. Would you like to try a simple breathing exercise or talk about what might be contributing to these feelings?"
            }
            userMessage.contains("anxious") || userMessage.contains("worried") || userMessage.contains("stress") -> {
                "Anxiety can be really challenging to manage. Let's focus on some grounding techniques that might help. Try taking 5 deep breaths - inhale for 4 counts, hold for 4, exhale for 6. How does that feel? We can also explore what's causing your anxiety if you'd like to talk about it."
            }
            userMessage.contains("happy") || userMessage.contains("good") || userMessage.contains("great") -> {
                "That's wonderful to hear! It's great that you're feeling positive. To help maintain this good energy, consider activities that reinforce these positive feelings - maybe some light exercise, connecting with friends, or doing something creative. What brings you joy?"
            }
            userMessage.contains("activity") || userMessage.contains("do") || userMessage.contains("suggestion") -> {
                "Great question! Based on your current state, here are some activities that might help: gentle walking, journaling your thoughts, listening to calming music, or trying a new hobby. What interests you most? I can provide more specific suggestions based on what you enjoy."
            }
            userMessage.contains("score") || userMessage.contains("happiness") || userMessage.contains("phi") -> {
                "Your happiness score gives us insight into your overall well-being. Remember, this is just a snapshot of how you're feeling right now. The goal isn't to always have a perfect score, but to understand your patterns and find ways to support your mental health journey."
            }
            else -> {
                "Thank you for sharing that with me. I'm here to listen and support you. Is there anything specific about your emotional well-being that you'd like to explore or discuss further?"
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 