package com.example.smartshuffle

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.smartshuffle.data.AppDatabase
import com.example.smartshuffle.data.SongRepositoryImpl
import com.example.smartshuffle.data.PlayHistoryDao
import com.example.smartshuffle.domain.RankingEngine
import com.example.smartshuffle.domain.SongRepository

interface AppContainer {
    val songRepository: SongRepository
    val rankingEngine: RankingEngine
    val playHistoryDao: PlayHistoryDao
    val userPreferences: com.example.smartshuffle.data.UserPreferences
}

class DefaultAppContainer(private val applicationContext: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "smartshuffle_database"
        ).build()
    }

    override val songRepository: SongRepository by lazy {
        SongRepositoryImpl(database.songDao(), database.queueAssociationDao(), applicationContext)
    }
    
    override val rankingEngine: RankingEngine by lazy {
        RankingEngine(database.rankDao(), database.playHistoryDao(), database.songDao(), database.queueAssociationDao())
    }
    
    override val playHistoryDao: PlayHistoryDao by lazy {
        database.playHistoryDao()
    }

    override val userPreferences: com.example.smartshuffle.data.UserPreferences by lazy {
        com.example.smartshuffle.data.UserPreferences(applicationContext)
    }
}

class SmartShuffleApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
