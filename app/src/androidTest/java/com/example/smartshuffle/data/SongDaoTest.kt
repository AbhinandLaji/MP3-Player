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
 * Instrumented tests for SongDao.
 * Covers upsert logic and folder grouping queries.
 */
@RunWith(AndroidJUnit4::class)
class SongDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        songDao = database.songDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun makeSong(
        id: Long = 0,
        title: String = "Test Song",
        filePath: String = "/music/test.mp3",
        artist: String = "Artist",
        album: String = "Album"
    ) = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = 200_000L,
        filePath = filePath,
        albumArtUri = null
    )

    // ═══════════════════════════════════════════════════════════════════
    // 1. Song Upsert Logic
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun insertingSongWithExistingFilePathDoesNotDuplicate() = runTest {
        // Insert first song
        val song1 = makeSong(title = "Original Title", filePath = "/music/same.mp3")
        val id1 = songDao.insert(song1)
        assertTrue("First insert should succeed", id1 > 0)

        // Try inserting another song with the SAME filePath
        val song2 = makeSong(title = "Duplicate Title", filePath = "/music/same.mp3")
        val id2 = songDao.insert(song2)

        // OnConflictStrategy.IGNORE should return -1 on conflict
        assertEquals("Second insert with same filePath should be ignored (return -1)", -1L, id2)

        // Verify only one song exists
        val allSongs = songDao.getAllSongsSync()
        assertEquals("Should have exactly 1 song, not 2", 1, allSongs.size)
        assertEquals("Original Title", allSongs[0].title)
    }

    @Test
    fun updateExistingSongByPathPreservesId() = runTest {
        // Insert original
        val original = makeSong(title = "Original", filePath = "/music/update_test.mp3")
        songDao.insert(original)

        // Find by path and update
        val existing = songDao.getSongByPath("/music/update_test.mp3")
        assertNotNull("Should find song by path", existing)

        val updated = existing!!.copy(title = "Updated Title", artist = "New Artist")
        songDao.update(updated)

        // Verify update
        val result = songDao.getSongByPath("/music/update_test.mp3")
        assertNotNull(result)
        assertEquals("Updated Title", result!!.title)
        assertEquals("New Artist", result.artist)
        assertEquals(existing.id, result.id)  // ID should be preserved
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Folder Grouping Query
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun folderGroupingReturnsCorrectSongCountsPerFolder() = runTest {
        // Insert songs in different folders
        songDao.insert(makeSong(title = "A1", filePath = "/music/rock/song1.mp3"))
        songDao.insert(makeSong(title = "A2", filePath = "/music/rock/song2.mp3"))
        songDao.insert(makeSong(title = "A3", filePath = "/music/rock/song3.mp3"))
        songDao.insert(makeSong(title = "B1", filePath = "/music/jazz/song1.mp3"))
        songDao.insert(makeSong(title = "C1", filePath = "/music/classical/song1.mp3"))
        songDao.insert(makeSong(title = "C2", filePath = "/music/classical/song2.mp3"))

        val allSongs = songDao.getAllSongs().first()
        assertEquals("Should have 6 songs total", 6, allSongs.size)

        // Group by folder path (same logic as SongRepositoryImpl)
        val folderGroups = allSongs.groupBy {
            java.io.File(it.filePath).parentFile?.absolutePath ?: "Unknown"
        }

        // Verify folder counts
        val rockFolder = folderGroups.entries.find { it.key.endsWith("rock") }
        val jazzFolder = folderGroups.entries.find { it.key.endsWith("jazz") }
        val classicalFolder = folderGroups.entries.find { it.key.endsWith("classical") }

        assertNotNull("Rock folder should exist", rockFolder)
        assertNotNull("Jazz folder should exist", jazzFolder)
        assertNotNull("Classical folder should exist", classicalFolder)

        assertEquals("Rock folder should have 3 songs", 3, rockFolder!!.value.size)
        assertEquals("Jazz folder should have 1 song", 1, jazzFolder!!.value.size)
        assertEquals("Classical folder should have 2 songs", 2, classicalFolder!!.value.size)
    }

    @Test
    fun getAllSongsSyncReturnsAllInsertedSongs() = runTest {
        songDao.insert(makeSong(title = "S1", filePath = "/a.mp3"))
        songDao.insert(makeSong(title = "S2", filePath = "/b.mp3"))
        songDao.insert(makeSong(title = "S3", filePath = "/c.mp3"))

        val songs = songDao.getAllSongsSync()
        assertEquals(3, songs.size)
    }
}
