package com.example.smartshuffle.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for RankDao.
 * Covers insertOrUpdate replace behavior and null returns.
 */
@RunWith(AndroidJUnit4::class)
class RankDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var rankDao: RankDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        rankDao = database.rankDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertOrUpdateReplacesExistingRank() = runTest {
        val original = SongRank(songId = 1L, rankValue = 5f, lastUpdated = 1000L)
        rankDao.insertOrUpdate(original)

        val updated = SongRank(songId = 1L, rankValue = 15f, lastUpdated = 2000L)
        rankDao.insertOrUpdate(updated)

        val result = rankDao.getRank(1L)
        assertNotNull(result)
        assertEquals("Rank value should be updated", 15f, result!!.rankValue, 0.001f)
        assertEquals("Last updated should be updated", 2000L, result.lastUpdated)

        // Verify no duplicates
        val allRanks = rankDao.getAllRanks()
        assertEquals("Should have exactly 1 rank entry for songId=1", 1, allRanks.size)
    }

    @Test
    fun getRankReturnsNullForUnknownSongId() = runTest {
        val result = rankDao.getRank(999L)
        assertNull("getRank should return null for unknown songId", result)
    }

    @Test
    fun getAllRanksReturnsMultipleEntries() = runTest {
        rankDao.insertOrUpdate(SongRank(songId = 1L, rankValue = 5f, lastUpdated = 1000L))
        rankDao.insertOrUpdate(SongRank(songId = 2L, rankValue = 10f, lastUpdated = 2000L))
        rankDao.insertOrUpdate(SongRank(songId = 3L, rankValue = 15f, lastUpdated = 3000L))

        val allRanks = rankDao.getAllRanks()
        assertEquals(3, allRanks.size)
    }
}
