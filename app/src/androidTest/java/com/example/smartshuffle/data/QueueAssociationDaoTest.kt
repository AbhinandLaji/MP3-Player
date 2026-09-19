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
 * Instrumented tests for QueueAssociationDao.
 * Covers upsert behavior and count incrementing.
 */
@RunWith(AndroidJUnit4::class)
class QueueAssociationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var queueDao: QueueAssociationDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        queueDao = database.queueAssociationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ═══════════════════════════════════════════════════════════════════
    // 1. First call creates a new row
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun incrementAssociationCreatesRowOnFirstCall() = runTest {
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)

        val associations = queueDao.getAssociationsForSong(1L)
        assertEquals("Should have exactly 1 association", 1, associations.size)
        assertEquals(1L, associations[0].songIdA)
        assertEquals(2L, associations[0].songIdB)
        assertEquals("First call should set count to 1", 1, associations[0].count)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Same pair increments count, no duplicate
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun sameAssociationPairIncrementsCountNoDuplicate() = runTest {
        // Call 3 times for the same pair
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)

        val associations = queueDao.getAssociationsForSong(1L)
        assertEquals("Should still have exactly 1 association (no duplicates)", 1, associations.size)
        assertEquals("Count should be 3 after 3 increments", 3, associations[0].count)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 3. Different pairs are tracked independently
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun differentPairsTrackedIndependently() = runTest {
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 3L)

        val associations = queueDao.getAssociationsForSong(1L)
        assertEquals("Should have 2 different associations", 2, associations.size)

        val assocTo2 = associations.find { it.songIdB == 2L }
        val assocTo3 = associations.find { it.songIdB == 3L }

        assertNotNull(assocTo2)
        assertNotNull(assocTo3)
        assertEquals("Association to song 2 should have count 2", 2, assocTo2!!.count)
        assertEquals("Association to song 3 should have count 1", 1, assocTo3!!.count)
    }

    @Test
    fun associationsAreDirectional() = runTest {
        // A->B is different from B->A
        queueDao.incrementAssociationCount(songIdA = 1L, songIdB = 2L)
        queueDao.incrementAssociationCount(songIdA = 2L, songIdB = 1L)

        val assocFrom1 = queueDao.getAssociationsForSong(1L)
        val assocFrom2 = queueDao.getAssociationsForSong(2L)

        assertEquals(1, assocFrom1.size)
        assertEquals(1, assocFrom2.size)
        assertEquals(2L, assocFrom1[0].songIdB)
        assertEquals(1L, assocFrom2[0].songIdB)
    }
}
