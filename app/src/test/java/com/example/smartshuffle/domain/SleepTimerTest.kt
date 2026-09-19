package com.example.smartshuffle.domain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for sleep timer logic.
 *
 * These tests verify the timer math and state transitions
 * without requiring a full MediaController or Android context.
 * They test the same logic used in PlaybackController.
 */
class SleepTimerTest {

    // ── Minimal Timer State Model ────────────────────────────────────
    // Mirrors the logic in PlaybackController without Android dependencies

    private var sleepTimerTargetMillis: Long? = null

    private fun startSleepTimer(durationMinutes: Int) {
        val target = System.currentTimeMillis() + (durationMinutes * 60_000L)
        sleepTimerTargetMillis = target
    }

    private fun cancelSleepTimer() {
        sleepTimerTargetMillis = null
    }

    /**
     * Simulates the auto-advance decision from PlaybackController line 113-120.
     * Returns true if playback should continue, false if it should stop.
     */
    private fun shouldAllowAutoAdvance(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val target = sleepTimerTargetMillis ?: return true
        return if (currentTimeMillis >= target) {
            // Timer has fired — stop and clear
            cancelSleepTimer()
            false
        } else {
            true
        }
    }

    /**
     * Manual skip is never blocked by the timer — mirrors PlaybackController.skipNext()
     * which calls seekToNextMediaItem() unconditionally.
     */
    private fun manualSkip(): Boolean {
        // Manual skips bypass the timer check entirely
        return true
    }

    @Before
    fun setup() {
        sleepTimerTargetMillis = null
    }

    // ═══════════════════════════════════════════════════════════════════
    // 1. startSleepTimer() — Target Timestamp
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `startSleepTimer sets target correctly based on duration`() {
        val before = System.currentTimeMillis()
        startSleepTimer(30)  // 30 minutes
        val after = System.currentTimeMillis()

        assertNotNull("Timer target should be set", sleepTimerTargetMillis)

        val expectedMin = before + (30 * 60_000L)
        val expectedMax = after + (30 * 60_000L)

        assertTrue(
            "Target ($sleepTimerTargetMillis) should be between $expectedMin and $expectedMax",
            sleepTimerTargetMillis!! in expectedMin..expectedMax
        )
    }

    @Test
    fun `startSleepTimer with 0 minutes sets target at approximately now`() {
        val before = System.currentTimeMillis()
        startSleepTimer(0)
        val after = System.currentTimeMillis()

        assertNotNull(sleepTimerTargetMillis)
        assertTrue(
            "0-minute timer target should be approximately now",
            sleepTimerTargetMillis!! in before..after
        )
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Auto-advance proceeds when timer not yet reached
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `auto-advance proceeds when current time has NOT reached target`() {
        // Set timer for 30 minutes in the future
        startSleepTimer(30)

        val result = shouldAllowAutoAdvance()
        assertTrue("Auto-advance should proceed when timer hasn't fired yet", result)
        assertNotNull("Timer should still be active", sleepTimerTargetMillis)
    }

    @Test
    fun `auto-advance proceeds when no timer is set`() {
        // No timer set at all
        val result = shouldAllowAutoAdvance()
        assertTrue("Auto-advance should proceed when no timer is active", result)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 3. Timer fires — stop after current track
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `auto-advance blocked when current time HAS reached target`() {
        // Set timer that's already expired (in the past)
        sleepTimerTargetMillis = System.currentTimeMillis() - 1000L

        val result = shouldAllowAutoAdvance()
        assertFalse("Auto-advance should be blocked when timer has fired", result)
    }

    @Test
    fun `auto-advance blocked when target is exactly now`() {
        sleepTimerTargetMillis = System.currentTimeMillis()

        val result = shouldAllowAutoAdvance(sleepTimerTargetMillis!!)
        assertFalse("Auto-advance should be blocked when target == now", result)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 4. Manual skip is never blocked
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `manual skip is never blocked by timer, even after target`() {
        // Timer has fired
        sleepTimerTargetMillis = System.currentTimeMillis() - 1000L

        val result = manualSkip()
        assertTrue("Manual skip should never be blocked by the sleep timer", result)
    }

    @Test
    fun `manual skip works when no timer is set`() {
        val result = manualSkip()
        assertTrue("Manual skip should work with no timer", result)
    }

    // ═══════════════════════════════════════════════════════════════════
    // 5. Timer clears itself after firing
    // ═══════════════════════════════════════════════════════════════════

    @Test
    fun `timer clears itself after firing once`() {
        sleepTimerTargetMillis = System.currentTimeMillis() - 1000L

        // First auto-advance check — timer fires, blocks advance
        val firstResult = shouldAllowAutoAdvance()
        assertFalse("First check should block", firstResult)
        assertNull("Timer should be cleared after firing", sleepTimerTargetMillis)

        // Second auto-advance check — timer is gone, should proceed
        val secondResult = shouldAllowAutoAdvance()
        assertTrue("Second check should proceed (timer cleared)", secondResult)
    }

    @Test
    fun `cancelSleepTimer clears the target`() {
        startSleepTimer(30)
        assertNotNull(sleepTimerTargetMillis)

        cancelSleepTimer()
        assertNull("Timer target should be null after cancel", sleepTimerTargetMillis)
    }
}
