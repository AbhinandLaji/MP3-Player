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
        SongRepositoryImpl(database.songDao(), applicationContext)
    }
    
    override val rankingEngine: RankingEngine by lazy {
        RankingEngine(database.rankDao(), database.playHistoryDao(), database.songDao())
    }
    
    override val playHistoryDao: PlayHistoryDao by lazy {
        database.playHistoryDao()
    }
}

class SmartShuffleApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
