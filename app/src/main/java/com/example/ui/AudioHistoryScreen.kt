package com.example.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.AudioHistoryEntity
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioHistoryScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val history by viewModel.historyList.collectAsState()

    var renameDialogItem by remember { mutableStateOf<AudioHistoryEntity?>(null) }
    var newTitleInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUDIO HISTORY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Your generated voice recordings",
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
            if (history.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearAllHistory() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = ErrorRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No generated audio history yet.", color = TextGray)
                    Text("Generate your first voice audio from Home.", color = NeonCyan, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history, key = { it.id }) { item ->
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
                                Text(
                                    text = item.title,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(onClick = {
                                        renameDialogItem = item
                                        newTitleInput = item.title
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextGray, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteHistory(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.text,
                                color = TextGray,
                                fontSize = 13.sp,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(item.createdAt))
                                Text(dateStr, color = TextGray, fontSize = 11.sp)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Share Button
                                    IconButton(
                                        onClick = {
                                            val file = File(item.filePath)
                                            if (file.exists()) {
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "audio/wav"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Share Audio"))
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    }

                                    // Play Button
                                    Button(
                                        onClick = { viewModel.playAudio(item.filePath) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextWhite, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Play", fontSize = 12.sp, color = TextWhite)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (renameDialogItem != null) {
        AlertDialog(
            onDismissRequest = { renameDialogItem = null },
            title = { Text("Rename Audio Title", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = newTitleInput,
                    onValueChange = { newTitleInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = BorderGlow,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    renameDialogItem?.let { item ->
                        if (newTitleInput.isNotBlank()) {
                            viewModel.renameHistory(item.id, newTitleInput.trim())
                        }
                    }
                    renameDialogItem = null
                }) {
                    Text("Save", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogItem = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
