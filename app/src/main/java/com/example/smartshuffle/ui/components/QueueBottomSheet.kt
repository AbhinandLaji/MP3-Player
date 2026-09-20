package com.example.smartshuffle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.screens.LibraryViewModel
import com.example.smartshuffle.ui.theme.*
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val queue by viewModel.currentQueue.collectAsState()

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
            if (queue.isEmpty()) {
                Text(
                    text = "Queue is empty",
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Section 1: NOW PLAYING (Pinned)
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonRed,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                NowPlayingCard(song = queue[0])

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: NEXT IN QUEUE
                val originalUpNext = remember(queue) {
                    queue.drop(1).mapIndexed { index, song ->
                        "${song.id}_$index" to song
                    }
                }
                
                if (originalUpNext.isNotEmpty()) {
                    Text(
                        text = "NEXT IN QUEUE",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    val lazyListState = rememberLazyListState()
                    
                    // Maintain a local mutable copy of the list so the UI can update instantly during drag
                    // but don't overwrite it with new StateFlow emissions while a drag is actively happening.
                    var upNext by remember { mutableStateOf(originalUpNext) }
                    
                    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                        upNext = upNext.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                        
                        viewModel.moveQueueItem(from.index, to.index)
                    }

                    // Sync local list with ExoPlayer list when not dragging
                    LaunchedEffect(originalUpNext, reorderableState.isAnyItemDragging) {
                        if (!reorderableState.isAnyItemDragging) {
                            upNext = originalUpNext
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        itemsIndexed(upNext, key = { _, pair -> pair.first }) { index, pair ->
                            val song = pair.second
                            ReorderableItem(
                                state = reorderableState,
                                key = pair.first
                            ) { isDragging ->
                                // The item itself is wrapped in SwipeToDismiss
                                val elevation = if (isDragging) 8.dp else 0.dp
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(elevation)
                                ) {
                                    SwipeToDismissQueueItem(
                                        song = song,
                                        displayIndex = index + 1,
                                        onDelete = { viewModel.removeFromQueue(index) },
                                        dragHandleModifier = Modifier.draggableHandle()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlayingCard(song: Song) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    Surface(
        color = NeonSurfaceHi,
        shape = CutCornerShape6,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = borderAlpha)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(4.dp, spotColor = NeonRed, ambientColor = NeonRed)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(NeonRed, shape = androidx.compose.foundation.shape.CircleShape)
                    .shadow(4.dp, spotColor = NeonRed, ambientColor = NeonRed)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = NeonRed,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissQueueItem(
    song: Song,
    displayIndex: Int,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                NeonRed.copy(alpha = 0.5f)
            } else Color.Transparent
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        content = {
            QueueItemRowDraggable(
                index = displayIndex,
                song = song,
                dragHandleModifier = dragHandleModifier
            )
        }
    )
}

@Composable
fun QueueItemRowDraggable(
    index: Int,
    song: Song,
    dragHandleModifier: Modifier
) {
    Surface(
        color = NeonSurface,
        shape = CutCornerShape6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = NeonSurfaceHi,
                shape = CutCornerShape6,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$index",
                        color = NeonBlue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontFamily = ChakraPetch)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = NeonBlue,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = ChakraPetch
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Drag handle with the provided modifier
            Icon(
                imageVector = Icons.Default.DragHandle, 
                contentDescription = "Reorder", 
                tint = NeonBlue.copy(alpha = 0.6f),
                modifier = dragHandleModifier
                    .padding(8.dp)
            )
        }
    }
}
