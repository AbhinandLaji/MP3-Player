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
                FolderRow(folder = folder) {
                    viewModel.setSelectedFolder(folder.folderPath)
                    navController.navigate(FolderDetailRoute)
                }
            }
        }
    }
}

@Composable
fun FolderRow(folder: FolderSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = folder.folderName, style = MaterialTheme.typography.titleMedium)
            Text(text = folder.folderPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = "${folder.songCount} songs", style = MaterialTheme.typography.bodyMedium)
    }
}
