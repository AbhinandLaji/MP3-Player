package com.example.smartshuffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshuffle.ui.screens.LibraryViewModel
import com.example.smartshuffle.ui.theme.NeonBg
import com.example.smartshuffle.ui.theme.NeonBlue
import com.example.smartshuffle.ui.theme.NeonRed
import com.example.smartshuffle.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    onDismiss: () -> Unit,
    viewModel: LibraryViewModel
) {
    val sleepTimerTargetMillis by viewModel.sleepTimerTargetMillis.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NeonBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = NeonBlue) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SLEEP TIMER",
                color = NeonBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (sleepTimerTargetMillis != null) {
                var remainingMillis by remember { mutableLongStateOf(sleepTimerTargetMillis!! - System.currentTimeMillis()) }

                LaunchedEffect(sleepTimerTargetMillis) {
                    while (remainingMillis > 0) {
                        delay(1000)
                        remainingMillis = (sleepTimerTargetMillis!! - System.currentTimeMillis()).coerceAtLeast(0)
                    }
                }

                val minutes = (remainingMillis / 1000) / 60
                val seconds = (remainingMillis / 1000) % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)

                Text(
                    text = "SHUTDOWN IN: $timeString",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        viewModel.cancelSleepTimer()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = CutCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Text("CANCEL TIMER", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "TIMER INACTIVE",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Text(
                text = "PRESETS",
                color = NeonBlue.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )

            val presets = listOf(15, 30, 45, 60)
            presets.forEach { minutes ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(CutCornerShape(6.dp))
                        .background(Color(0xFF16202F))
                        .clickable {
                            viewModel.startSleepTimer(minutes)
                            onDismiss()
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "$minutes MINUTES",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
