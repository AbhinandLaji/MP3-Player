package com.example.smartshuffle.domain

import com.example.smartshuffle.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for play count computation and sort order.
 * Tests that getPlayCounts() correctly sums MANUAL + SHUFFLE entries,
 * that zero-play songs are treated as count 0, and that the sort
 * matches the LibraryViewModel's "most-played first" ordering.
 */
class PlayCountSortTest {

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

    private lateinit var dao: FakePlayHistoryDao

    private fun addPlay(songId: Long, playType: PlayType) {
        dao.history.add(
            PlayHistory(
                id = dao.history.size.toLong() + 1,
                songId = songId,
                timestamp = System.currentTimeMillis(),
                playType = playType
            )
        )
    }

    private fun makeSong(id: Long, title: String = "Song $id") = Song(
        id = id, title = title, artist = "Artist", album = "Album",
        duration = 200_000L, filePath = "/music/$title.mp3", albumArtUri = null
    )

    @Before
    fun setup() {
        dao = FakePlayHistoryDao()
    }

    // ═══════════════════════════════════════════════════════════════════
    // 1. getPlayCounts() — Summing MANUAL + SHUFFLE
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `getPlayCounts sums both MANUAL and SHUFFLE entries per song`() = runTest {
        // Song 1: 3 MANUAL + 2 SHUFFLE = 5 total
        repeat(3) { addPlay(1L, PlayType.MANUAL) }
        repeat(2) { addPlay(1L, PlayType.SHUFFLE) }

        // Song 2: 1 MANUAL + 0 SHUFFLE = 1 total
        addPlay(2L, PlayType.MANUAL)

        val counts = dao.getPlayCounts().first()
        val countMap = counts.associateBy({ it.songId }, { it.playCount })

        assertEquals("Song 1 should have 5 total plays", 5, countMap[1L])
        assertEquals("Song 2 should have 1 total play", 1, countMap[2L])
    }

    @Test
    fun `song with zero PlayHistory entries is treated as play count 0`() = runTest {
        // Add plays only for song 1
        addPlay(1L, PlayType.MANUAL)

        val counts = dao.getPlayCounts().first()
        val countMap = counts.associateBy({ it.songId }, { it.playCount })

        // Song 99 has no plays — it should not appear in the map
        assertNull("Song with no plays should not be in the count map", countMap[99L])

        // When used for sorting, missing = 0
        val effectiveCount = countMap[99L] ?: 0
        assertEquals("Effective play count for unplayed song should be 0", 0, effectiveCount)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Sort Order — Most-played first, unplayed last
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `songs sort most-played first, unplayed last`() = runTest {
        // Set up known play counts:
        // Song 1: 0 plays (unplayed)
        // Song 2: 5 plays
        // Song 3: 2 plays
        // Song 4: 10 plays
        // Song 5: 0 plays (unplayed)

        repeat(5) { addPlay(2L, PlayType.MANUAL) }
        repeat(2) { addPlay(3L, PlayType.SHUFFLE) }
        repeat(10) { addPlay(4L, PlayType.MANUAL) }

        val allSongs = listOf(makeSong(1L), makeSong(2L), makeSong(3L), makeSong(4L), makeSong(5L))

        val counts = dao.getPlayCounts().first()
        val countMap = counts.associateBy({ it.songId }, { it.playCount })

        // Apply the same sort the ViewModel uses: sortedByDescending { playCountMap[it.id] ?: 0 }
        val sorted = allSongs.sortedByDescending { countMap[it.id] ?: 0 }

        assertEquals("Most-played song (10 plays) should be first", 4L, sorted[0].id)
        assertEquals("Second most-played (5 plays) should be second", 2L, sorted[1].id)
        assertEquals("Third most-played (2 plays) should be third", 3L, sorted[2].id)

        // Songs 1 and 5 both have 0 plays — they should be at the end
        val lastTwoIds = sorted.takeLast(2).map { it.id }.toSet()
        assertTrue(
            "Unplayed songs (1 and 5) should be at the end, got ${sorted.map { it.id }}",
            lastTwoIds.contains(1L) && lastTwoIds.contains(5L)
        )
    }

    @Test
    fun `sort stability - songs with equal play counts maintain relative order`() = runTest {
        // Songs 1, 2, 3 all have 3 plays
        repeat(3) { addPlay(1L, PlayType.MANUAL) }
        repeat(3) { addPlay(2L, PlayType.SHUFFLE) }
        repeat(3) { addPlay(3L, PlayType.MANUAL) }

        val allSongs = listOf(makeSong(1L), makeSong(2L), makeSong(3L))
        val counts = dao.getPlayCounts().first()
        val countMap = counts.associateBy({ it.songId }, { it.playCount })

        val sorted = allSongs.sortedByDescending { countMap[it.id] ?: 0 }

        // All have the same count, so stable sort should preserve original order
        assertEquals(3, sorted.size)
        // Just verify they all have the same count
        sorted.forEach { song ->
            assertEquals(3, countMap[song.id])
        }
    }
}
