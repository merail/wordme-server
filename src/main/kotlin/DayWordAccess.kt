package app

import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

internal class DayWordAccess(
    private val gameCountdownStartDate: LocalDate,
    private val clock: Clock = Clock.systemUTC(),
) {

    private companion object {
        // Pacific/Kiritimati: the earliest time zone, so no honest player anywhere gets rejected.
        val EARLIEST_ZONE_OFFSET: ZoneOffset = ZoneOffset.ofHours(14)

        val CLOCK_SKEW_ALLOWANCE: Duration = Duration.ofMinutes(5)
    }

    fun isAvailable(
        dayWordId: Int,
    ): Boolean {
        val earliestToday = clock.instant()
            .plus(CLOCK_SKEW_ALLOWANCE)
            .atOffset(EARLIEST_ZONE_OFFSET)
            .toLocalDate()

        return dayWordId <= ChronoUnit.DAYS.between(gameCountdownStartDate, earliestToday) + 1
    }
}
