package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Text(
            text = "SETTINGS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Configure app preferences and storage",
            fontSize = 13.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dark Theme", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("AMOLED & futuristic dark palette", color = TextGray, fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = ElectricPurple
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGlow)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ElectricPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Default Audio Engine", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Android Native TTS Engine", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGlow)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Clear All Audio History", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Permanently delete stored audio records", color = TextGray, fontSize = 12.sp)
                        }
                    }
                    TextButton(onClick = { showClearDialog = true }) {
                        Text("Clear", color = ErrorRed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy & Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Privacy & Security", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI Voice Studio processes text and speech synthesis locally on your Android device using built-in system TTS engines. Your text data is never uploaded to external servers without your direct consent.",
                    color = TextGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderGlow)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version", color = TextGray, fontSize = 13.sp)
                    Text("1.0.0 (Production)", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History", color = TextWhite) },
            text = { Text("Are you sure you want to delete all saved audio history records? This action cannot be undone.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    showClearDialog = false
                }) {
                    Text("Delete All", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
