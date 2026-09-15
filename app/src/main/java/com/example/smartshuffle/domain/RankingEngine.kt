package com.example.smartshuffle.domain

import com.example.smartshuffle.data.PlayHistory
import com.example.smartshuffle.data.PlayHistoryDao
import com.example.smartshuffle.data.PlayType
import com.example.smartshuffle.data.RankDao
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.data.SongDao
import com.example.smartshuffle.data.SongRank
import kotlin.random.Random

class RankingEngine(
    private val rankDao: RankDao,
    private val playHistoryDao: PlayHistoryDao,
    private val songDao: SongDao
) {
    private val SHUFFLE_RANK_INCREMENT = 10f
    private val MANUAL_RANK_INCREMENT = SHUFFLE_RANK_INCREMENT / 2f
    private val RANK_DECAY_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // 3 days

    suspend fun incrementRank(songId: Long, playType: PlayType) {
        val increment = if (playType == PlayType.SHUFFLE) SHUFFLE_RANK_INCREMENT else MANUAL_RANK_INCREMENT
        val currentRank = rankDao.getRank(songId)
        
        val newRankValue = if (currentRank != null) {
            getEffectiveRank(currentRank) + increment
        } else {
            increment
        }
        
        rankDao.insertOrUpdate(SongRank(songId, newRankValue, System.currentTimeMillis()))
        playHistoryDao.insert(PlayHistory(songId = songId, timestamp = System.currentTimeMillis(), playType = playType))
    }

    private suspend fun getEffectiveRank(songId: Long): Float {
        val rank = rankDao.getRank(songId) ?: return 0f
        return getEffectiveRank(rank)
    }

    private fun getEffectiveRank(rank: SongRank): Float {
        val elapsed = System.currentTimeMillis() - rank.lastUpdated
        if (elapsed >= RANK_DECAY_PERIOD_MS) return 0f
        
        val decayFactor = 1f - (elapsed.toFloat() / RANK_DECAY_PERIOD_MS.toFloat())
        return rank.rankValue * decayFactor
    }

    suspend fun selectNextShuffleSong(excludeSongId: Long? = null): Song? {
        val allSongs = songDao.getAllSongsSync()
        val eligibleSongs = allSongs.filter { it.id != excludeSongId }
        
        if (eligibleSongs.isEmpty()) return allSongs.firstOrNull()

        val songsWithWeights = eligibleSongs.map { song ->
            val effectiveRank = getEffectiveRank(song.id)
            val weight = 1.0 / (1.0 + effectiveRank)
            song to weight
        }

        val totalWeight = songsWithWeights.sumOf { it.second }
        var randomValue = Random.nextDouble() * totalWeight

        for ((song, weight) in songsWithWeights) {
            randomValue -= weight
            if (randomValue <= 0) {
                return song
            }
        }
        
        return eligibleSongs.lastOrNull()
    }
}
