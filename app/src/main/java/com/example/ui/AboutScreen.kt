package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(2.dp, NeonCyan)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI VOICE STUDIO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "\"Your Voice, Every Language.\"",
            fontSize = 14.sp,
            color = NeonCyan,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, BorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About AI Voice Studio", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI Voice Studio is a state-of-the-art multilingual Text-to-Speech application built for Android. It converts written text into natural spoken audio supporting Urdu (Pakistan), Hindi (India), and English (US & UK) with male and female voice filters.",
                    color = TextGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderGlow)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Key Features", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FeatureRow(icon = Icons.Default.Language, title = "Multilingual Support", subtitle = "Urdu, Hindi, English (US/UK)")
                FeatureRow(icon = Icons.Default.RecordVoiceOver, title = "Voice Library", subtitle = "Male & Female voice filters")
                FeatureRow(icon = Icons.Default.Speed, title = "Speed & Pitch Controls", subtitle = "Fine-tune speaking rate and voice pitch")
                FeatureRow(icon = Icons.Default.Download, title = "WAV Export & Sharing", subtitle = "Save audio files to device and share instantly")
                FeatureRow(icon = Icons.Default.OfflineBolt, title = "Offline Capability", subtitle = "Works offline for installed device voices")
            }
        }
    }
}

@Composable
fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextGray, fontSize = 11.sp)
        }
    }
}
