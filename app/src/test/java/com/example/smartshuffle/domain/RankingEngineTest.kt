package com.example.smartshuffle.domain

import com.example.smartshuffle.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests for RankingEngine.
 * Uses fake DAO implementations — no Android dependencies needed.
 */
class RankingEngineTest {

    // ── Fake DAOs ────────────────────────────────────────────────────────

    private class FakeRankDao : RankDao {
        val ranks = mutableMapOf<Long, SongRank>()

        override fun getAllRanks(): List<SongRank> = ranks.values.toList()

        override suspend fun getRank(songId: Long): SongRank? = ranks[songId]

        override suspend fun insertOrUpdate(songRank: SongRank) {
            ranks[songRank.songId] = songRank
        }
    }

    private class FakePlayHistoryDao : PlayHistoryDao {
        val history = mutableListOf<PlayHistory>()

        override fun getAllHistory(): List<PlayHistory> = history.toList()

        override fun getPlayCounts(): Flow<List<PlayCount>> {
            val counts = history.groupBy { it.songId }
                .map { (songId, entries) -> PlayCount(songId, entries.size) }
            return flowOf(counts)
        }

        override suspend fun insert(playHistory: PlayHistory) {
            history.add(playHistory)
        }
    }

    private class FakeSongDao : SongDao {
        val songs = mutableListOf<Song>()

        override fun getAllSongs(): Flow<List<Song>> = flowOf(songs.toList())

        override suspend fun getAllSongsSync(): List<Song> = songs.toList()

        override suspend fun getSongByPath(path: String): Song? =
            songs.find { it.filePath == path }

        override suspend fun insert(song: Song): Long {
            songs.add(song)
            return song.id
        }

        override suspend fun update(song: Song) {
            val index = songs.indexOfFirst { it.id == song.id }
            if (index >= 0) songs[index] = song
        }
    }

    private class FakeQueueAssociationDao : QueueAssociationDao {
        val associations = mutableListOf<QueueAssociation>()

        override fun getAllAssociations(): List<QueueAssociation> = associations.toList()

        override suspend fun getAssociationsForSong(songId: Long): List<QueueAssociation> =
            associations.filter { it.songIdA == songId }

        override suspend fun incrementAssociationCount(songIdA: Long, songIdB: Long) {
            val existing = associations.find { it.songIdA == songIdA && it.songIdB == songIdB }
            if (existing != null) {
                associations.remove(existing)
                associations.add(existing.copy(count = existing.count + 1))
            } else {
                associations.add(QueueAssociation(songIdA, songIdB, 1))
            }
        }
    }

    // ── Test Setup ───────────────────────────────────────────────────────

    private lateinit var rankDao: FakeRankDao
    private lateinit var playHistoryDao: FakePlayHistoryDao
    private lateinit var songDao: FakeSongDao
    private lateinit var queueDao: FakeQueueAssociationDao
    private lateinit var engine: RankingEngine

    private fun makeSong(id: Long, title: String = "Song $id") = Song(
        id = id,
        title = title,
        artist = "Artist",
        album = "Album",
        duration = 200_000L,
        filePath = "/music/$title.mp3",
        albumArtUri = null
    )

    @Before
    fun setup() {
        rankDao = FakeRankDao()
        playHistoryDao = FakePlayHistoryDao()
        songDao = FakeSongDao()
        queueDao = FakeQueueAssociationDao()
        engine = RankingEngine(rankDao, playHistoryDao, songDao, queueDao)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 1. incrementRank() — Constant Relationship Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `SHUFFLE increment is exactly double MANUAL increment`() {
        // Test the actual constants, not hardcoded expected values
        assertEquals(
            "SHUFFLE_RANK_INCREMENT should be exactly 2x MANUAL_RANK_INCREMENT",
            engine.SHUFFLE_RANK_INCREMENT,
            engine.MANUAL_RANK_INCREMENT * 2f,
            0.001f
        )
    }

    @Test
    fun `incrementRank with MANUAL uses MANUAL_RANK_INCREMENT`() = runTest {
        engine.incrementRank(1L, PlayType.MANUAL)

        val storedRank = rankDao.ranks[1L]
        assertNotNull("Rank should be stored after MANUAL play", storedRank)
        assertEquals(
            engine.MANUAL_RANK_INCREMENT,
            storedRank!!.rankValue,
            0.001f
        )
    }

    @Test
    fun `incrementRank with SHUFFLE uses SHUFFLE_RANK_INCREMENT`() = runTest {
        engine.incrementRank(1L, PlayType.SHUFFLE)

        val storedRank = rankDao.ranks[1L]
        assertNotNull("Rank should be stored after SHUFFLE play", storedRank)
        assertEquals(
            engine.SHUFFLE_RANK_INCREMENT,
            storedRank!!.rankValue,
            0.001f
        )
    }

    @Test
    fun `incrementRank records PlayHistory entry`() = runTest {
        engine.incrementRank(1L, PlayType.MANUAL)
        engine.incrementRank(2L, PlayType.SHUFFLE)

        assertEquals(2, playHistoryDao.history.size)
        assertEquals(PlayType.MANUAL, playHistoryDao.history[0].playType)
        assertEquals(1L, playHistoryDao.history[0].songId)
        assertEquals(PlayType.SHUFFLE, playHistoryDao.history[1].playType)
        assertEquals(2L, playHistoryDao.history[1].songId)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. getEffectiveRank() — Decay Math Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `song with no SongRank entry returns effective rank 0`() = runTest {
        // No rank inserted for songId=99
        val effectiveRank = engine.getEffectiveRank(99L)
        assertEquals(0f, effectiveRank, 0.001f)
    }

    @Test
    fun `song ranked at t=0 returns full rankValue when queried at t=0`() {
        val now = System.currentTimeMillis()
        val rank = SongRank(songId = 1L, rankValue = 10f, lastUpdated = now)

        val effectiveRank = engine.getEffectiveRank(rank)

        // At elapsed ~ 0, decayFactor ~ 1.0, so effective ~ rankValue
        // Allow small tolerance for the few ms between now and the call
        assertTrue(
            "Effective rank at t=0 should be very close to full rankValue (got $effectiveRank)",
            effectiveRank >= 9.9f
        )
    }

    @Test
    fun `song ranked at t=0 returns half rankValue at t = 1_5 days`() {
        val decayPeriod = engine.RANK_DECAY_PERIOD_MS
        val halfDecay = decayPeriod / 2  // 1.5 days

        val now = System.currentTimeMillis()
        val rank = SongRank(songId = 1L, rankValue = 10f, lastUpdated = now - halfDecay)

        val effectiveRank = engine.getEffectiveRank(rank)

        // At elapsed = halfDecay, decayFactor = 1 - (halfDecay / decayPeriod) = 0.5
        // Expected = 10 * 0.5 = 5.0
        assertEquals(
            "Effective rank at half the decay period should be exactly half",
            5.0f,
            effectiveRank,
            0.05f  // Small tolerance for ms-level timing
        )
    }

    @Test
    fun `song ranked at t=0 returns effective rank 0 at t = 3 days`() {
        val decayPeriod = engine.RANK_DECAY_PERIOD_MS
        val now = System.currentTimeMillis()

        val rank = SongRank(songId = 1L, rankValue = 10f, lastUpdated = now - decayPeriod)
        val effectiveRank = engine.getEffectiveRank(rank)

        assertEquals(
            "Effective rank at t >= 3 days should be exactly 0",
            0f,
            effectiveRank,
            0.001f
        )
    }

    @Test
    fun `song ranked at t=0 returns effective rank 0 when well past decay period`() {
        val decayPeriod = engine.RANK_DECAY_PERIOD_MS
        val now = System.currentTimeMillis()

        // 5 days ago (well past 3-day decay)
        val rank = SongRank(songId = 1L, rankValue = 10f, lastUpdated = now - (5 * 24 * 60 * 60 * 1000L))
        val effectiveRank = engine.getEffectiveRank(rank)

        assertEquals(0f, effectiveRank, 0.001f)
    }

    @Test
    fun `re-play before full decay stacks correctly on decaying value`() = runTest {
        val decayPeriod = engine.RANK_DECAY_PERIOD_MS

        // Simulate: song was played 1 day ago via SHUFFLE (rank = 10)
        val oneDayMs = 24 * 60 * 60 * 1000L
        val playTime = System.currentTimeMillis() - oneDayMs

        // Pre-seed the rank as if it was set 1 day ago
        rankDao.ranks[1L] = SongRank(songId = 1L, rankValue = 10f, lastUpdated = playTime)

        // Calculate what the effective rank should be right now (before re-play)
        val elapsed = System.currentTimeMillis() - playTime
        val decayFactor = 1f - (elapsed.toFloat() / decayPeriod.toFloat())
        val decayedValue = 10f * decayFactor  // ~ 10 * (1 - 1/3) ~ 6.67

        // Now re-play the song via MANUAL (should stack on top of decayed value)
        engine.incrementRank(1L, PlayType.MANUAL)

        val newRank = rankDao.ranks[1L]
        assertNotNull(newRank)

        // Expected: decayedValue + MANUAL_RANK_INCREMENT
        val expectedNewRankValue = decayedValue + engine.MANUAL_RANK_INCREMENT
        assertEquals(
            "New rank should be decayed old value + MANUAL increment",
            expectedNewRankValue,
            newRank!!.rankValue,
            0.2f  // Tolerance for timing between calculations
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // 3. selectNextShuffleSong() — Weighted Selection Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `weighted selection favors rank-0 song over high-rank songs`() = runTest {
        // Set up 5 songs
        val songs = (1L..5L).map { makeSong(it) }
        songs.forEach { songDao.songs.add(it) }

        // Song 1: effective rank 0 (no SongRank entry)
        // Songs 2-5: high effective rank (recently played, high value)
        val now = System.currentTimeMillis()
        for (id in 2L..5L) {
            rankDao.ranks[id] = SongRank(songId = id, rankValue = 50f, lastUpdated = now)
        }

        // Run selection 2000 times, using songId=99 as "current" (excluded)
        val counts = mutableMapOf<Long, Int>()
        val iterations = 2000
        for (i in 0 until iterations) {
            val selected = engine.selectNextShuffleSong(currentSongId = 99L)
            assertNotNull("selectNextShuffleSong should never return null with valid candidates", selected)
            counts[selected!!.id] = (counts[selected.id] ?: 0) + 1
        }

        val song1Count = counts[1L] ?: 0
        val otherCounts = (2L..5L).map { counts[it] ?: 0 }
        val avgOther = otherCounts.average()

        // Song 1 (rank 0) should be picked at least 2x the average of the high-rank songs
        assertTrue(
            "Rank-0 song was picked $song1Count times, avg of high-rank songs was $avgOther. " +
            "Expected rank-0 to be at least 2x the average.",
            song1Count >= avgOther * 2
        )
    }

    @Test
    fun `weighted selection picks every song at least occasionally with equal ranks`() = runTest {
        // All songs have rank 0 (no SongRank entry) — equal base weights.
        // With equal weights, the ±20% jitter is the only differentiator,
        // so every song should be picked with roughly equal frequency.
        val songs = (1L..5L).map { makeSong(it) }
        songs.forEach { songDao.songs.add(it) }

        // No rank entries — all songs have effective rank 0

        val counts = mutableMapOf<Long, Int>()
        val iterations = 1000
        for (i in 0 until iterations) {
            val selected = engine.selectNextShuffleSong(currentSongId = 99L)
            assertNotNull(selected)
            counts[selected!!.id] = (counts[selected.id] ?: 0) + 1
        }

        // Every song should be picked at least once
        for (id in 1L..5L) {
            val count = counts[id] ?: 0
            assertTrue(
                "Song $id was never picked in $iterations iterations",
                count > 0
            )
        }
    }

    @Test
    fun `large rank disparity still allows occasional selection of high-rank songs`() = runTest {
        // With true weighted random sampling, a large rank disparity should drastically
        // favor the low-rank song, but the high-rank song MUST still be picked occasionally.
        //
        // rank-0: base=1.0
        // rank-30: base=0.032
        //
        // The ratio is roughly 30:1, so we expect the high rank song to be picked
        // roughly ~3% of the time.
        val songs = (1L..5L).map { makeSong(it) }
        songs.forEach { songDao.songs.add(it) }

        val now = System.currentTimeMillis()
        // Songs 1,4,5: rank 0 (no entry)
        // Songs 2,3: rank 30 (large disparity)
        rankDao.ranks[2L] = SongRank(songId = 2L, rankValue = 30f, lastUpdated = now)
        rankDao.ranks[3L] = SongRank(songId = 3L, rankValue = 30f, lastUpdated = now)

        val counts = mutableMapOf<Long, Int>()
        val iterations = 5000
        for (i in 0 until iterations) {
            val selected = engine.selectNextShuffleSong(currentSongId = 99L)
            assertNotNull(selected)
            counts[selected!!.id] = (counts[selected.id] ?: 0) + 1
        }

        val highRankPicks = (counts[2L] ?: 0) + (counts[3L] ?: 0)
        val lowRankPicks = (counts[1L] ?: 0) + (counts[4L] ?: 0) + (counts[5L] ?: 0)
        
        println("TEST STATS: Low rank picks: $lowRankPicks, High rank picks: $highRankPicks")
        
        assertTrue(
            "Low-rank songs ($lowRankPicks) should heavily dominate over high-rank songs ($highRankPicks)",
            lowRankPicks > highRankPicks * 5 // At least 5x more often
        )
        
        assertTrue(
            "High-rank songs MUST still be picked occasionally, got $highRankPicks picks in $iterations iterations",
            highRankPicks > 5 // Should get picked at least a handful of times
        )
    }

    @Test
    fun `excludeSongId is never selected`() = runTest {
        val songs = (1L..5L).map { makeSong(it) }
        songs.forEach { songDao.songs.add(it) }

        // Exclude song 3 (it's the "current song")
        val excludedId = 3L
        val iterations = 200
        for (i in 0 until iterations) {
            val selected = engine.selectNextShuffleSong(currentSongId = excludedId)
            assertNotNull(selected)
            assertNotEquals(
                "Excluded song (currentSongId=$excludedId) was selected - this should never happen",
                excludedId,
                selected!!.id
            )
        }
    }

    @Test
    fun `selectNextShuffleSong returns null when no valid candidates`() = runTest {
        // Only one song, and it's excluded
        songDao.songs.add(makeSong(1L))

        val result = engine.selectNextShuffleSong(currentSongId = 1L)
        assertNull("Should return null when only candidate is excluded", result)
    }

    @Test
    fun `selectNextShuffleSong returns null when library is empty`() = runTest {
        val result = engine.selectNextShuffleSong(currentSongId = 1L)
        assertNull("Should return null with empty library", result)
    }

    @Test
    fun `selectNextShuffleSong respects playlistContext scope`() = runTest {
        // Global library has songs 1-5
        val allSongs = (1L..5L).map { makeSong(it) }
        allSongs.forEach { songDao.songs.add(it) }

        // But playlist context only has songs 1-2
        val playlistContext = listOf(makeSong(1L), makeSong(2L))

        val iterations = 100
        for (i in 0 until iterations) {
            val selected = engine.selectNextShuffleSong(
                currentSongId = 99L,
                playlistContext = playlistContext
            )
            assertNotNull(selected)
            assertTrue(
                "Selected song ${selected!!.id} is not in the playlist context",
                selected.id == 1L || selected.id == 2L
            )
        }
    }
}
