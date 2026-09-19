package com.example.smartshuffle

import android.Manifest
import android.os.Build
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartshuffle.playback.PlaybackController
import com.example.smartshuffle.ui.navigation.FolderDetailRoute
import com.example.smartshuffle.ui.navigation.FoldersRoute
import com.example.smartshuffle.ui.navigation.LibraryRoute
import com.example.smartshuffle.ui.navigation.NowPlayingRoute
import com.example.smartshuffle.ui.screens.LibraryScreen
import com.example.smartshuffle.ui.screens.LibraryViewModel
import com.example.smartshuffle.ui.screens.NowPlayingScreen
import com.example.smartshuffle.ui.theme.SmartShuffleTheme

class MainActivity : ComponentActivity() {

    private val _shouldOpenNowPlaying = MutableStateFlow(false)
    val shouldOpenNowPlaying: StateFlow<Boolean> = _shouldOpenNowPlaying.asStateFlow()

    private lateinit var playbackController: PlaybackController
    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        checkIntent(intent)
        
        val app = application as SmartShuffleApplication
        playbackController = PlaybackController(this, app.container.rankingEngine, app.container.userPreferences)

        checkAndRequestPermission()

        enableEdgeToEdge()
        setContent {
            SmartShuffleTheme {
                var showBootScreen by remember { mutableStateOf(true) }

                if (showBootScreen) {
                    com.example.smartshuffle.ui.screens.CyberAudioBootScreen(
                        onFinished = { showBootScreen = false }
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (hasPermission) {
                            val factory = LibraryViewModel.provideFactory(app, playbackController)
                            val viewModel: LibraryViewModel = viewModel(factory = factory)

                            val navController = rememberNavController()
                            val currentBackStack by navController.currentBackStackEntryAsState()
                            val currentDestination = currentBackStack?.destination

                            val openNowPlaying by shouldOpenNowPlaying.collectAsState()

                            LaunchedEffect(openNowPlaying) {
                                if (openNowPlaying) {
                                    navController.navigate(NowPlayingRoute) {
                                        launchSingleTop = true
                                    }
                                    _shouldOpenNowPlaying.value = false // Reset state flag after routing
                                }
                            }

                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                            androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                                    NavHost(navController = navController, startDestination = com.example.smartshuffle.ui.navigation.HomeRoute) {
                                        composable<com.example.smartshuffle.ui.navigation.HomeRoute> {
                                            com.example.smartshuffle.ui.screens.HomeScreen(viewModel = viewModel, navController = navController)
                                        }
                                        composable<FolderDetailRoute> {
                                            com.example.smartshuffle.ui.screens.FolderDetailScreen(viewModel = viewModel, navController = navController)
                                        }
                                        composable<NowPlayingRoute> {
                                            NowPlayingScreen(viewModel = viewModel, navController = navController)
                                        }
                                    }
                                }
                            }
                            
                            com.example.smartshuffle.ui.components.CyberpunkOverlay()
                        }
                    } else {
                        PermissionDeniedScreen {
                            checkAndRequestPermission()
                        }
                    }
                }
                }
            }
        }
    }
    
    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        if (checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(permission)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("OPEN_NOW_PLAYING", false) == true) {
            _shouldOpenNowPlaying.value = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackController.release()
    }
}

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Permission is required to access your music.")
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

