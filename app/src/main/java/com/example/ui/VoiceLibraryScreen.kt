package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLibraryScreen(viewModel: MainViewModel) {
    val availableVoices by viewModel.ttsManager.availableVoices.collectAsState()
    var selectedLangFilter by remember { mutableStateOf("All") }
    var selectedGenderFilter by remember { mutableStateOf("All") }

    val languages = listOf("All", "English", "Urdu", "Hindi")
    val genders = listOf("All", "Male", "Female", "Unknown")

    val filteredVoices = availableVoices.filter { voice ->
        val langMatch = selectedLangFilter == "All" || voice.languageDisplayName.contains(selectedLangFilter, ignoreCase = true) || voice.locale.language.contains(selectedLangFilter, ignoreCase = true)
        val genderMatch = selectedGenderFilter == "All" || voice.gender.equals(selectedGenderFilter, ignoreCase = true)
        langMatch && genderMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Text(
            text = "VOICE LIBRARY",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Browse installed neural & standard TTS voices on device",
            fontSize = 13.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Language Filters
        Text("Filter by Language", fontSize = 12.sp, color = NeonCyan)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(languages) { lang ->
                val isSelected = selectedLangFilter == lang
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLangFilter = lang },
                    label = { Text(lang) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricPurple,
                        selectedLabelColor = TextWhite,
                        containerColor = CardDark,
                        labelColor = TextGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gender Filters
        Text("Filter by Gender", fontSize = 12.sp, color = NeonCyan)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(genders) { gender ->
                val isSelected = selectedGenderFilter == gender
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGenderFilter = gender },
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

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredVoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matching voices found.", color = TextGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVoices) { voice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, BorderGlow)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = voice.name,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Language: ${voice.languageDisplayName} (${voice.locale})",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(containerColor = SurfaceDark) {
                                        Text(voice.gender, color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Badge(containerColor = SurfaceDark) {
                                        val availability = if (voice.isNetworkRequired) "Online" else "Offline Ready"
                                        Text(availability, color = if (voice.isNetworkRequired) PurpleAccent else SuccessGreen, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.previewVoice(voice) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = TextWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview", fontSize = 12.sp, color = TextWhite)
                            }
                        }
                    }
                }
            }
        }
    }
}
