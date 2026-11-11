package app

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DayWordAccessTest {

    private fun dayWordAccess(
        now: String,
    ) = DayWordAccess(
        gameCountdownStartDate = LocalDate.of(2025, 7, 14),
        clock = Clock.fixed(Instant.parse(now), ZoneOffset.UTC),
    )

    @Test
    fun `today and past words are available`() {
        val access = dayWordAccess("2025-07-14T00:00:00Z")

        assertTrue(access.isAvailable(dayWordId = 0))
        assertTrue(access.isAvailable(dayWordId = 1))
        assertFalse(access.isAvailable(dayWordId = 2))
    }

    @Test
    fun `next word opens with midnight in UTC+14 minus clock skew allowance`() {
        assertFalse(dayWordAccess("2025-07-14T09:54:59Z").isAvailable(dayWordId = 2))
        assertTrue(dayWordAccess("2025-07-14T09:55:00Z").isAvailable(dayWordId = 2))
        assertFalse(dayWordAccess("2025-07-14T09:55:00Z").isAvailable(dayWordId = 3))
    }
}
