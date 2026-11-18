package com.example.ui

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.*
import com.example.viewmodel.GenerationState
import com.example.viewmodel.MainViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedGenderFilter by viewModel.selectedGenderFilter.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val availableVoices by viewModel.ttsManager.availableVoices.collectAsState()
    val speed by viewModel.speechSpeed.collectAsState()
    val pitch by viewModel.speechPitch.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currentAudioPath by viewModel.currentAudioPath.collectAsState()
    val currentAudioTitle by viewModel.currentAudioTitle.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val currentTime by viewModel.currentPlaybackTime.collectAsState()
    val totalTime by viewModel.totalDurationTime.collectAsState()

    val scrollState = rememberScrollState()

    // Filter voices based on selected gender & language
    val filteredVoices = availableVoices.filter { voice ->
        val langMatch = voice.locale.language == selectedLanguage.language
        val genderMatch = selectedGenderFilter == "All" || voice.gender.equals(selectedGenderFilter, ignoreCase = true)
        langMatch && genderMatch
    }

    // Sound wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title & Tagline
        Text(
            text = "AI VOICE STUDIO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\"Your Voice, Every Language.\"",
            fontSize = 14.sp,
            color = NeonCyan,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Animated Sound Wave Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderGlow, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = 32
                val barWidth = canvasWidth / (barCount * 1.5f)
                val spacing = barWidth * 0.5f

                for (i in 0 until barCount) {
                    val multiplier = ((i % 5) + 1) * 0.2f
                    val heightFactor = (0.3f + (waveAnim * multiplier)).coerceIn(0.1f, 1f)
                    val barH = canvasHeight * heightFactor * 0.7f
                    val x = i * (barWidth + spacing)
                    val y = (canvasHeight - barH) / 2f

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonCyan, ElectricPurple)
                        ),
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barH)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language Selector
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Select Language", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            val languages = listOf(
                "English (US)" to Locale.US,
                "English (UK)" to Locale.UK,
                "Urdu" to Locale("ur", "PK"),
                "Hindi" to Locale("hi", "IN")
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { (label, locale) ->
                    val isSelected = selectedLanguage.language == locale.language
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setLanguage(locale) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple,
                            selectedLabelColor = TextWhite,
                            containerColor = CardDark,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else BorderGlow
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gender / Voice Type Selector
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Voice Type", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            val genders = listOf("All", "Male", "Female")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                genders.forEach { gender ->
                    val isSelected = selectedGenderFilter == gender
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGenderFilter(gender) },
                        label = { Text(gender) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = BgDark,
                            containerColor = CardDark,
                            labelColor = TextGray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Picker Dropdown
        if (filteredVoices.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Installed Voice", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                val currentVoiceName = selectedVoice?.name ?: filteredVoices.first().name

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, BorderGlow, RoundedCornerShape(12.dp))
                        .clickable { expanded = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentVoiceName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Gender: ${selectedVoice?.gender ?: filteredVoices.first().gender}", color = NeonCyan, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Voice", tint = TextGray)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        filteredVoices.forEach { voice ->
                            DropdownMenuItem(
                                text = { Text("${voice.name} (${voice.gender})", color = TextWhite) },
                                onClick = {
                                    viewModel.setSelectedVoice(voice)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Text Editor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Text to Speech", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row {
                        IconButton(onClick = { viewModel.setInputText("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Text", tint = TextGray)
                        }
                        IconButton(onClick = {
                            viewModel.setInputText("Welcome to AI Voice Studio. Convert your text into natural sounding speech instantly in Urdu, Hindi, and English.")
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Load Sample", tint = NeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.setInputText(it) },
                    placeholder = { Text("Type or paste your text here...", color = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = BorderGlow,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${inputText.length} chars",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    if (selectedLanguage.language == "ur") {
                        Text("RTL Urdu Enabled", color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed & Pitch Sliders Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Voice Controls", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Speaking Speed: ${String.format(Locale.US, "%.1fx", speed)}", color = TextGray, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { s ->
                            Button(
                                onClick = { viewModel.setSpeed(s) },
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (speed == s) ElectricPurple else SurfaceDark
                                )
                            ) {
                                Text("${s}x", fontSize = 11.sp, color = TextWhite)
                            }
                        }
                    }
                }
                Slider(
                    value = speed,
                    onValueChange = { viewModel.setSpeed(it) },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricPurple
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Voice Pitch: ${String.format(Locale.US, "%.1fx", pitch)}", color = TextGray, fontSize = 13.sp)
                }
                Slider(
                    value = pitch,
                    onValueChange = { viewModel.setPitch(it) },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricPurple
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Generate Audio Button
        Button(
            onClick = { viewModel.generateAudio() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
            enabled = generationState != GenerationState.GENERATING
        ) {
            if (generationState == GenerationState.GENERATING) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextWhite, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Generating Voice...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            } else {
                Icon(Icons.Default.Mic, contentDescription = null, tint = TextWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE AUDIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite, letterSpacing = 1.sp)
            }
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                color = if (generationState == GenerationState.ERROR) ErrorRed else NeonCyan,
                fontSize = 13.sp
            )
        }

        // Audio Player Card (When audio is generated/available)
        if (currentAudioPath != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, NeonCyan)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(currentAudioTitle.ifEmpty { "Generated Audio.wav" }, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(onClick = { viewModel.stopAudio() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Player", tint = TextGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seekbar / Progress
                    Slider(
                        value = progress,
                        onValueChange = { viewModel.seekTo(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(currentTime, color = TextGray, fontSize = 12.sp)
                        Text(totalTime, color = TextGray, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playback Controls & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(ElectricPurple, RoundedCornerShape(24.dp))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = TextWhite
                            )
                        }

                        // Download / Save to Device
                        IconButton(
                            onClick = {
                                currentAudioPath?.let { path ->
                                    val file = File(path)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Save or Share AI Voice"))
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(CardDark, RoundedCornerShape(24.dp))
                                .border(1.dp, BorderGlow, RoundedCornerShape(24.dp))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download / Save", tint = NeonCyan)
                        }

                        // Share Audio
                        IconButton(
                            onClick = {
                                currentAudioPath?.let { path ->
                                    val file = File(path)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TEXT, "Generated with AI Voice Studio - Your Voice, Every Language.")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share AI Voice"))
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(CardDark, RoundedCornerShape(24.dp))
                                .border(1.dp, BorderGlow, RoundedCornerShape(24.dp))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = TextWhite)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
