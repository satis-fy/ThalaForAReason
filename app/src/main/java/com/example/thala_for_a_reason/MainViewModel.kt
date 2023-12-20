package com.example.thala_for_a_reason

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun generateGeminiContent(prompt: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _isLoading.value = true

            val generativeModel = GenerativeModel(
                "gemini-pro",
                apiKey = "API_KEY",
                generationConfig = generationConfig {
                    temperature = 1f
                    topK = 1
                    topP = 1f
                    maxOutputTokens = 2048
                },
                safetySettings = listOf(
                    SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
                    SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
                    SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
                    SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
                ),
            )

            val inputContent = content {
                text("Create a link to the number 7 from a given input by manipulating characters—adding or subtracting—resulting in a clever and engaging connection. Keep it short and sweet for a good user experience!")
                text("input: 2023")
                text("output: 2+0+2+3 = 7")
                text("input: YouTube")
                text("output: YouTube has 7 letters So the output will be 7")
                text("input: Pizza")
                text("output: Pizza has 7 slices")
                text("input: Jio")
                text("output: Jio turned 7 this year")
                text("input: Apple iPhone is the best")
                text("output: Apple's iPhone is widely recognized for its quality and features. If we consider the word quality, it has 7 letters. Therefore, the output is 7.")
                text("input: MacBook")
                text("output: The word Macbook has 7 letters.")
                text("input: 58.4k followers")
                text("output: 5+8/4= 7")
                text("input: neighbor is having a crazy party")
                text("output: If we consider the word neighbor, it has 7 letters.")
                text("input: men will be men")
                text("output: The phrase men will be men has 7 words")
                text("input: coding is a really fun thing")
                text("output: If we consider the word coding, it has 6 letters. Adding 1 to represent the word is, we get 7.")
                text("input: UNKNOWN")
                text("output: If we consider the word UNKNOWN, it has 7 letters.")
                text("input: why did Twitter change its name?")
                text("output: Twitter's tweets, a 7-letter")
                text("input: I love reaction videos on YouTube")
                text("output: Enjoying reaction videos on YouTube, where YouTube itself has 7 letters.")
                text("input: Monday")
                text("output: Mondays: 7 letters of dread,")
                text("input: $prompt")
                text("output: ")
            }

            val response = generativeModel.generateContent(inputContent)

            _isLoading.value = false
            _content.value =
                response.candidates.first().content.parts.first().asTextOrNull().toString()
        }
    }
}
