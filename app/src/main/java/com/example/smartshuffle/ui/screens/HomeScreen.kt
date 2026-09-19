package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.smartshuffle.ui.navigation.NowPlayingRoute
import com.example.smartshuffle.ui.theme.ChakraPetch
import com.example.smartshuffle.ui.theme.NeonBg
import com.example.smartshuffle.ui.theme.NeonBlue
import com.example.smartshuffle.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerDefaults

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: LibraryViewModel, navController: NavController) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    
    val customFling = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            currentSong?.let { song ->
                NowPlayingBar(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onClick = { navController.navigate(NowPlayingRoute) }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = NeonBlue
                    )
                },
                containerColor = NeonBg
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Songs", fontFamily = ChakraPetch, color = if (pagerState.currentPage == 0) NeonBlue else TextSecondary) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Folders", fontFamily = ChakraPetch, color = if (pagerState.currentPage == 1) NeonBlue else TextSecondary) }
                )
            }
            
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                flingBehavior = customFling,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LibraryScreen(viewModel = viewModel, navController = navController)
                    1 -> FoldersScreen(viewModel = viewModel, navController = navController)
                }
            }
        }
    }
}
