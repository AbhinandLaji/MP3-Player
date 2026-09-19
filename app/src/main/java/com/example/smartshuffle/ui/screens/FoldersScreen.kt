package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.smartshuffle.domain.FolderSummary
import com.example.smartshuffle.ui.navigation.FolderDetailRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import com.example.smartshuffle.ui.theme.NeonRed
import com.example.smartshuffle.ui.theme.NeonBlue
import com.example.smartshuffle.ui.theme.NeonSurfaceHi
import com.example.smartshuffle.ui.theme.TextSecondary
import com.example.smartshuffle.ui.theme.ChakraPetch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import com.example.smartshuffle.ui.theme.CutCornerShape6
import androidx.compose.foundation.border

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldersScreen(viewModel: LibraryViewModel, navController: NavController, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    val folders by viewModel.folders.collectAsState()
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 80.dp // To prevent overlap with NowPlayingBar
        )
    ) {
        items(folders, key = { it.folderPath }) { folder ->
            FolderRow(
                folder = folder,
                modifier = Modifier.animateItemPlacement(),
                onClick = {
                    viewModel.setSelectedFolder(folder.folderPath)
                    navController.navigate(FolderDetailRoute)
                },
                onToggleFavorite = {
                    viewModel.toggleFavoriteFolder(folder.folderPath)
                }
            )
        }
    }
}

@Composable
fun FolderRow(
    folder: FolderSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(NeonSurfaceHi.copy(alpha = 0.5f), CutCornerShape6)
            .border(1.dp, NeonBlue.copy(alpha = 0.1f), CutCornerShape6)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = NeonBlue,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.folderName, 
                    fontFamily = ChakraPetch,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.songCount} songs • ${folder.folderPath.takeLast(20)}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dedicated Favorite Star Toggle
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (folder.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (folder.isFavorite) "Unpin from favorites" else "Pin as favorite",
                        tint = if (folder.isFavorite) NeonRed else NeonRed.copy(alpha = 0.6f)
                    )
                }

                // Three-dot Options Menu
                IconButton(onClick = { /* folder overflow menu / options */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Folder options",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
