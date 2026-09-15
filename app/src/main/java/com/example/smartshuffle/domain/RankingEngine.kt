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
    private val songDao: SongDao,
    private val queueDao: com.example.smartshuffle.data.QueueAssociationDao
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

    private suspend fun calculateBaseWeight(song: Song): Double {
        val effectiveRank = getEffectiveRank(song.id)
        return 1.0 / (1.0 + effectiveRank)
    }

    suspend fun selectNextShuffleSong(
        currentSongId: Long,
        playlistContext: List<Song>? = null // New parameter!
    ): Song? {
        // 1. Establish the scoped candidate pool
        val candidatePool = if (!playlistContext.isNullOrEmpty()) {
            playlistContext
        } else {
            songDao.getAllSongsSync() // Your existing global fallback
        }

        // 2. Filter out the currently playing song
        val validCandidates = candidatePool.filter { it.id != currentSongId }

        if (validCandidates.isEmpty()) return null

        // 3. Fetch associations (Phase 4 logic)
        val associations = queueDao.getAssociationsForSong(currentSongId)
            .associateBy({ it.songIdB }, { it.count })

        var bestSong: Song? = null
        var highestWeight = -1.0

        // 4. Loop through the *scoped* validCandidates instead of allSongs
        for (song in validCandidates) {
            var weight = calculateBaseWeight(song) 

            val associationScore = associations[song.id]
            if (associationScore != null) {
                val multiplier = 1.0 + (associationScore * 0.15) 
                weight *= multiplier
            }

            weight *= (0.8 + Math.random() * 0.4) 

            if (weight > highestWeight) {
                highestWeight = weight
                bestSong = song
            }
        }
        return bestSong
    }
}
