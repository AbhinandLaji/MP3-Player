package com.example.smartshuffle.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.screens.LibraryViewModel
import com.example.smartshuffle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val queue by viewModel.currentQueue.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NeonBg,
        shape = CutCornerShape6,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "CURRENT QUEUE",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )

            if (queue.isEmpty()) {
                Text(
                    text = "Queue is empty",
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    itemsIndexed(queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                        val isPlaying = song.id == currentSong?.id
                        QueueItemRow(
                            index = index,
                            song = song,
                            isPlaying = isPlaying,
                            onMoveUp = if (index > 0) { { viewModel.moveQueueItem(index, index - 1) } } else null,
                            onMoveDown = if (index < queue.size - 1) { { viewModel.moveQueueItem(index, index + 1) } } else null,
                            onDelete = { viewModel.removeFromQueue(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItemRow(
    index: Int,
    song: Song,
    isPlaying: Boolean,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onDelete: () -> Unit
) {
    val titleColor = if (isPlaying) NeonRed else NeonBlue
    val containerColor = if (isPlaying) NeonSurfaceHi else NeonSurface

    Surface(
        color = containerColor,
        shape = CutCornerShape6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(32.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = titleColor,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (isPlaying) Modifier.shadow(2.dp, spotColor = NeonRed, ambientColor = NeonRed) else Modifier
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (onMoveUp != null) TextPrimary else TextSecondary.copy(alpha=0.3f))
                }
                IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (onMoveDown != null) TextPrimary else TextSecondary.copy(alpha=0.3f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = NeonRed)
                }
            }
        }
    }
}
