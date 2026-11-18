package com.example.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AudioHistoryEntity
import com.example.data.AudioHistoryRepository
import com.example.tts.TextToSpeechManager
import com.example.tts.VoiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class GenerationState {
    IDLE, GENERATING, SUCCESS, ERROR
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AudioHistoryRepository
    val historyList: StateFlow<List<AudioHistoryEntity>>

    val ttsManager = TextToSpeechManager(application)

    // UI Input State
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Locale.US)
    val selectedLanguage: StateFlow<Locale> = _selectedLanguage.asStateFlow()

    private val _selectedGenderFilter = MutableStateFlow("All") // "All", "Male", "Female"
    val selectedGenderFilter: StateFlow<String> = _selectedGenderFilter.asStateFlow()

    private val _selectedVoice = MutableStateFlow<VoiceInfo?>(null)
    val selectedVoice: StateFlow<VoiceInfo?> = _selectedVoice.asStateFlow()

    private val _speechSpeed = MutableStateFlow(1.0f)
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    // Generation State
    private val _generationState = MutableStateFlow(GenerationState.IDLE)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _currentAudioPath = MutableStateFlow<String?>(null)
    val currentAudioPath: StateFlow<String?> = _currentAudioPath.asStateFlow()

    private val _currentAudioTitle = MutableStateFlow("")
    val currentAudioTitle: StateFlow<String> = _currentAudioTitle.asStateFlow()

    // MediaPlayer state
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentPlaybackTime = MutableStateFlow("0:00")
    val currentPlaybackTime: StateFlow<String> = _currentPlaybackTime.asStateFlow()

    private val _totalDurationTime = MutableStateFlow("0:00")
    val totalDurationTime: StateFlow<String> = _totalDurationTime.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        val current = mp.currentPosition
                        val duration = mp.duration
                        if (duration > 0) {
                            _playbackProgress.value = current.toFloat() / duration.toFloat()
                            _currentPlaybackTime.value = formatTime(current)
                            _totalDurationTime.value = formatTime(duration)
                        }
                        handler.postDelayed(this, 200)
                    }
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error updating playback progress", e)
                }
            }
        }
    }

    init {
        val dao = AppDatabase.getDatabase(application).audioHistoryDao()
        repository = AudioHistoryRepository(dao)
        historyList = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setLanguage(locale: Locale) {
        _selectedLanguage.value = locale
        ttsManager.setLanguage(locale)
        // Auto-select first matching voice
        viewModelScope.launch {
            val voices = ttsManager.availableVoices.value
            val match = voices.firstOrNull { it.locale.language == locale.language }
            if (match != null) {
                _selectedVoice.value = match
                match.rawVoice?.let { ttsManager.setVoice(it) }
            }
        }
    }

    fun setGenderFilter(gender: String) {
        _selectedGenderFilter.value = gender
    }

    fun setSelectedVoice(voice: VoiceInfo?) {
        _selectedVoice.value = voice
        voice?.rawVoice?.let { ttsManager.setVoice(it) }
        voice?.locale?.let { 
            _selectedLanguage.value = it
            ttsManager.setLanguage(it)
        }
    }

    fun setSpeed(speed: Float) {
        _speechSpeed.value = speed
        ttsManager.setSpeechRate(speed)
    }

    fun setPitch(pitch: Float) {
        _speechPitch.value = pitch
        ttsManager.setPitch(pitch)
    }

    fun generateAudio() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) {
            _statusMessage.value = "Please enter text to generate speech."
            _generationState.value = GenerationState.ERROR
            return
        }

        _generationState.value = GenerationState.GENERATING
        _statusMessage.value = "Synthesizing audio..."

        ttsManager.setSpeechRate(_speechSpeed.value)
        ttsManager.setPitch(_speechPitch.value)
        _selectedVoice.value?.rawVoice?.let { ttsManager.setVoice(it) }
        ttsManager.setLanguage(_selectedLanguage.value)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appDir = File(getApplication<Application>().filesDir, "AI_Voice_Studio")
                if (!appDir.exists()) appDir.mkdirs()

                val timestamp = System.currentTimeMillis()
                val audioFile = File(appDir, "audio_$timestamp.wav")

                val result = ttsManager.synthesizeToFile(text, audioFile, "synth_$timestamp")
                if (result == TextToSpeech.SUCCESS) {
                    // Give it a short moment to flush file write
                    Thread.sleep(300)
                    if (audioFile.exists() && audioFile.length() > 0) {
                        val duration = getAudioDuration(audioFile.absolutePath)
                        val title = if (text.length > 25) text.substring(0, 25) + "..." else text
                        val voiceName = _selectedVoice.value?.name ?: "Default Voice"
                        val gender = _selectedVoice.value?.gender ?: "Unknown"

                        val entity = AudioHistoryEntity(
                            title = title,
                            text = text,
                            languageCode = _selectedLanguage.value.toString(),
                            voiceName = voiceName,
                            gender = gender,
                            speed = _speechSpeed.value,
                            pitch = _speechPitch.value,
                            filePath = audioFile.absolutePath,
                            durationMs = duration,
                            createdAt = timestamp
                        )

                        repository.insert(entity)

                        handler.post {
                            _currentAudioPath.value = audioFile.absolutePath
                            _currentAudioTitle.value = title
                            _generationState.value = GenerationState.SUCCESS
                            _statusMessage.value = "Audio generated successfully!"
                            playAudio(audioFile.absolutePath)
                        }
                    } else {
                        handler.post {
                            _generationState.value = GenerationState.ERROR
                            _statusMessage.value = "Error: Synthesized audio file is empty."
                        }
                    }
                } else {
                    handler.post {
                        _generationState.value = GenerationState.ERROR
                        _statusMessage.value = "TTS Synthesis failed with code $result"
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Generation error", e)
                handler.post {
                    _generationState.value = GenerationState.ERROR
                    _statusMessage.value = "Error: ${e.localizedMessage}"
                }
            }
        }
    }

    private fun getAudioDuration(path: String): Long {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(path)
            mp.prepare()
            val dur = mp.duration.toLong()
            mp.release()
            dur
        } catch (e: Exception) {
            3000L // fallback 3s
        }
    }

    fun playAudio(path: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                _isPlaying.value = true
                _totalDurationTime.value = formatTime(duration)
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackProgress.value = 0f
                    handler.removeCallbacks(progressRunnable)
                }
            }
            handler.post(progressRunnable)
            _currentAudioPath.value = path
        } catch (e: Exception) {
            Log.e("MainViewModel", "Playback error", e)
            _statusMessage.value = "Playback error: ${e.localizedMessage}"
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                handler.removeCallbacks(progressRunnable)
            } else {
                mp.start()
                _isPlaying.value = true
                handler.post(progressRunnable)
            }
        } ?: run {
            _currentAudioPath.value?.let { playAudio(it) }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying || _isPlaying.value) {
                mp.stop()
                mp.reset()
            }
        }
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _currentPlaybackTime.value = "0:00"
        handler.removeCallbacks(progressRunnable)
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.let { mp ->
            val target = (progress * mp.duration).toInt()
            mp.seekTo(target)
            _playbackProgress.value = progress
        }
    }

    fun previewVoice(voice: VoiceInfo) {
        setSelectedVoice(voice)
        val sampleText = when (voice.locale.language) {
            "ur" -> "AI Voice Studio mein khush amdeed."
            "hi" -> "AI Voice Studio mein aapka swagat hai."
            else -> "Welcome to AI Voice Studio."
        }
        ttsManager.speak(sampleText)
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
        }
    }

    fun renameHistory(id: Long, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rename(id, newTitle)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%d:%02d", mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        ttsManager.shutdown()
        handler.removeCallbacks(progressRunnable)
    }
}
