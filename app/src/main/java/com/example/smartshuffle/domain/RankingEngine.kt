package com.example.smartshuffle.domain

import com.example.smartshuffle.data.PlayHistory
import com.example.smartshuffle.data.PlayHistoryDao
import com.example.smartshuffle.data.PlayType
import com.example.smartshuffle.data.RankDao
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.data.SongDao
import com.example.smartshuffle.data.SongRank
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class RankingEngine(
    private val rankDao: RankDao,
    private val playHistoryDao: PlayHistoryDao,
    private val songDao: SongDao,
    private val queueDao: com.example.smartshuffle.data.QueueAssociationDao
) {
    internal val SHUFFLE_RANK_INCREMENT = 10f
    internal val MANUAL_RANK_INCREMENT = SHUFFLE_RANK_INCREMENT / 2f
    internal val RANK_DECAY_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L // 3 days

    private val rankCache = ConcurrentHashMap<Long, SongRank>()

    suspend fun initialize() {
        val ranks = rankDao.getAllRanks()
        ranks.forEach { rankCache[it.songId] = it }
    }

    suspend fun incrementRank(songId: Long, playType: PlayType) {
        val increment = if (playType == PlayType.SHUFFLE) SHUFFLE_RANK_INCREMENT else MANUAL_RANK_INCREMENT
        val currentRank = rankCache[songId]
        
        val newRankValue = if (currentRank != null) {
            getEffectiveRank(currentRank) + increment
        } else {
            increment
        }
        
        val newRank = SongRank(songId, newRankValue, System.currentTimeMillis())
        rankDao.insertOrUpdate(newRank)
        rankCache[songId] = newRank
        playHistoryDao.insert(PlayHistory(songId = songId, timestamp = System.currentTimeMillis(), playType = playType))
    }

    internal fun getEffectiveRank(songId: Long): Float {
        val rank = rankCache[songId] ?: return 0f
        return getEffectiveRank(rank)
    }

    internal fun getEffectiveRank(rank: SongRank): Float {
        val elapsed = System.currentTimeMillis() - rank.lastUpdated
        if (elapsed >= RANK_DECAY_PERIOD_MS) return 0f
        
        val decayFactor = 1f - (elapsed.toFloat() / RANK_DECAY_PERIOD_MS.toFloat())
        return rank.rankValue * decayFactor
    }

    private fun calculateBaseWeight(song: Song): Double {
        val effectiveRank = getEffectiveRank(song.id)
        return 1.0 / (1.0 + effectiveRank)
    }

    suspend fun selectNextShuffleSong(
        currentSongId: Long,
        excludedSongIds: Set<Long>,
        playlistContext: List<Song>? = null // New parameter!
    ): Song? {
        val startTime = System.currentTimeMillis()
        
        // 1. Establish the scoped candidate pool
        val candidatePool = if (!playlistContext.isNullOrEmpty()) {
            playlistContext
        } else {
            songDao.getAllSongsSync() // Your existing global fallback
        }

        // 2. Filter out the excluded songs
        val validCandidates = candidatePool.filter { it.id !in excludedSongIds }

        if (validCandidates.isEmpty()) {
            val duration = System.currentTimeMillis() - startTime
            return null
        }

        // 3. Fetch associations (Phase 4 logic)
        val associations = queueDao.getAssociationsForSong(currentSongId)
            .associateBy({ it.songIdB }, { it.count })

        // 4. Calculate weights for all valid candidates
        val candidateWeights = validCandidates.map { song ->
            var weight = calculateBaseWeight(song) 

            val associationScore = associations[song.id]
            if (associationScore != null) {
                val multiplier = 1.0 + (associationScore * 0.15) 
                weight *= multiplier
            }
            Pair(song, weight)
        }

        // 5. True weighted random sampling (roulette-wheel selection)
        val totalWeight = candidateWeights.sumOf { it.second }
        if (totalWeight <= 0.0) return validCandidates.randomOrNull()

        var randomValue = Random.nextDouble(totalWeight)
        for ((song, weight) in candidateWeights) {
            randomValue -= weight
            if (randomValue <= 0.0) {
                val duration = System.currentTimeMillis() - startTime
                return song
            }
        }
        
        // Fallback in case of floating point inaccuracies
        val duration = System.currentTimeMillis() - startTime
        return candidateWeights.lastOrNull()?.first
    }
}
