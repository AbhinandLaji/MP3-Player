package com.example.smartshuffle.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PlayHistoryDao.
 * Covers getPlayCounts aggregation and empty state.
 */
@RunWith(AndroidJUnit4::class)
class PlayHistoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var playHistoryDao: PlayHistoryDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playHistoryDao = database.playHistoryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun makeHistory(songId: Long, playType: PlayType) = PlayHistory(
        songId = songId,
        timestamp = System.currentTimeMillis(),
        playType = playType
    )

    // ═══════════════════════════════════════════════════════════════════
    // 1. getPlayCounts() sums both MANUAL and SHUFFLE
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun getPlayCountsSumsBothPlayTypes() = runTest {
        // Song 1: 3 MANUAL + 2 SHUFFLE = 5 total
        repeat(3) { playHistoryDao.insert(makeHistory(1L, PlayType.MANUAL)) }
        repeat(2) { playHistoryDao.insert(makeHistory(1L, PlayType.SHUFFLE)) }

        // Song 2: 1 SHUFFLE only
        playHistoryDao.insert(makeHistory(2L, PlayType.SHUFFLE))

        val counts = playHistoryDao.getPlayCounts().first()
        val countMap = counts.associateBy({ it.songId }, { it.playCount })

        assertEquals("Song 1 should have 5 total plays", 5, countMap[1L])
        assertEquals("Song 2 should have 1 total play", 1, countMap[2L])
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Empty history returns empty list
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun emptyHistoryReturnsEmptyPlayCounts() = runTest {
        val counts = playHistoryDao.getPlayCounts().first()
        assertTrue("Play counts should be empty with no history", counts.isEmpty())
    }

    // ═══════════════════════════════════════════════════════════════════
    // 3. Rescan does not duplicate PlayHistory
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun insertingHistoryDoesNotAutoGenDuplicateIds() = runTest {
        playHistoryDao.insert(makeHistory(1L, PlayType.MANUAL))
        playHistoryDao.insert(makeHistory(1L, PlayType.MANUAL))

        val allHistory = playHistoryDao.getAllHistory()
        assertEquals("Each insert should create a separate entry", 2, allHistory.size)

        // Verify auto-generated IDs are unique
        val ids = allHistory.map { it.id }.toSet()
        assertEquals("All IDs should be unique", 2, ids.size)
    }

    @Test
    fun multiplePlayTypesForSameSongTrackedCorrectly() = runTest {
        playHistoryDao.insert(makeHistory(1L, PlayType.MANUAL))
        playHistoryDao.insert(makeHistory(1L, PlayType.SHUFFLE))
        playHistoryDao.insert(makeHistory(1L, PlayType.MANUAL))

        val allHistory = playHistoryDao.getAllHistory()
        assertEquals(3, allHistory.size)

        val manualCount = allHistory.count { it.playType == PlayType.MANUAL }
        val shuffleCount = allHistory.count { it.playType == PlayType.SHUFFLE }

        assertEquals("Should have 2 MANUAL entries", 2, manualCount)
        assertEquals("Should have 1 SHUFFLE entry", 1, shuffleCount)
    }
}
