package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

data class VoiceInfo(
    val name: String,
    val locale: Locale,
    val languageDisplayName: String,
    val gender: String, // "Male", "Female", "Unknown"
    val isNetworkRequired: Boolean,
    val rawVoice: Voice?
)

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceInfo>>(emptyList())
    val availableVoices: StateFlow<List<VoiceInfo>> = _availableVoices.asStateFlow()

    private val _statusMessage = MutableStateFlow("Initializing TTS engine...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isInitialized.value = true
            _statusMessage.value = "TTS Engine Ready"
            loadVoices()
        } else {
            _isInitialized.value = false
            _statusMessage.value = "TTS Initialization Failed (Status: $status)"
            Log.e("TextToSpeechManager", "Initialization failed with status: $status")
        }
    }

    private fun loadVoices() {
        val ttsInstance = tts ?: return
        try {
            val voices = ttsInstance.voices ?: emptySet()
            val voiceList = mutableListOf<VoiceInfo>()

            for (voice in voices) {
                val locale = voice.locale ?: Locale.getDefault()
                val name = voice.name ?: "Unknown Voice"
                val lowerName = name.lowercase()
                
                // Determine gender heuristically from voice name
                val gender = when {
                    lowerName.contains("female") || lowerName.contains("woman") || lowerName.contains("#female") || lowerName.contains("f01") || lowerName.contains("f02") -> "Female"
                    lowerName.contains("male") || lowerName.contains("man") || lowerName.contains("#male") || lowerName.contains("m01") || lowerName.contains("m02") -> "Male"
                    else -> "Unknown"
                }

                val isNetwork = voice.isNetworkConnectionRequired

                voiceList.add(
                    VoiceInfo(
                        name = name,
                        locale = locale,
                        languageDisplayName = locale.getDisplayName(),
                        gender = gender,
                        isNetworkRequired = isNetwork,
                        rawVoice = voice
                    )
                )
            }

            // If no voices returned or set is empty, add fallback standard locales
            if (voiceList.isEmpty()) {
                val defaultLocales = listOf(
                    Locale("ur", "PK"),
                    Locale("hi", "IN"),
                    Locale.US,
                    Locale.UK
                )
                for (loc in defaultLocales) {
                    voiceList.add(
                        VoiceInfo(
                            name = "Default ${loc.displayLanguage}",
                            locale = loc,
                            languageDisplayName = loc.displayLanguage,
                            gender = "Unknown",
                            isNetworkRequired = false,
                            rawVoice = null
                        )
                    )
                }
            }

            _availableVoices.value = voiceList.sortedBy { it.languageDisplayName }
        } catch (e: Exception) {
            Log.e("TextToSpeechManager", "Error loading voices", e)
        }
    }

    fun setLanguage(locale: Locale): Boolean {
        val ttsInstance = tts ?: return false
        val result = ttsInstance.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun setVoice(voice: Voice?) {
        if (voice != null) {
            tts?.voice = voice
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun speak(text: String, utteranceId: String = "tts_utterance"): Int {
        val ttsInstance = tts ?: return TextToSpeech.ERROR
        return ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun synthesizeToFile(text: String, file: File, utteranceId: String = "tts_synth"): Int {
        val ttsInstance = tts ?: return TextToSpeech.ERROR
        return ttsInstance.synthesizeToFile(text, null, file, utteranceId)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
