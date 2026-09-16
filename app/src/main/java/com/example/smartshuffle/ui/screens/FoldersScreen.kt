package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartshuffle.domain.FolderSummary
import com.example.smartshuffle.ui.navigation.FolderDetailRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import com.example.smartshuffle.ui.theme.NeonRed
import com.example.smartshuffle.ui.theme.NeonBlue
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldersScreen(viewModel: LibraryViewModel, navController: NavController) {
    val folders by viewModel.folders.collectAsState()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
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
}

@Composable
fun FolderRow(
    folder: FolderSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = folder.folderName, style = MaterialTheme.typography.titleMedium)
            Text(text = folder.folderPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "${folder.songCount} songs", 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (folder.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (folder.isFavorite) "Unpin folder" else "Pin folder",
                tint = if (folder.isFavorite) NeonRed else NeonBlue.copy(alpha = 0.5f)
            )
        }
    }
}
