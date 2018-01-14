package inego.tinyscheduling.tests

import inego.tinyscheduling.Calendar
import inego.tinyscheduling.isWeekend

import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.*

private fun d(m: Int, d: Int) = LocalDate.of(2018, m, d)!!

val startingDate = d(1, 15)

fun calendarFactory() = Calendar(startingDate, ::isWeekend)


class CalendarTest {

    lateinit var calendar: Calendar

    @Before
    fun beforeEach() {
        calendar = calendarFactory()
    }

    @Test
    fun testNew() {
        assertEquals(startingDate, calendar.intToDate(0))
        assertEquals(0, calendar.dateToInt(startingDate))
    }

    @Test
    fun testNextDateFromInt() {
        assertEquals(d(1, 16), calendar.intToDate(1))
    }

    @Test
    fun testNextDateFromDate() {
        assertEquals(1, calendar.dateToInt(d(1, 16)))
    }

    @Test
    fun testExceptionOnHolidayToInt() {
        assertFails {
            print(calendar.dateToInt(d(1, 20)))
        }
    }

    @Test
    fun testSkipWeekendToInt() {
        assertEquals(5, calendar.dateToInt(d(1, 22)))
    }

    @Test
    fun testSkipWeekendToDate() {
        assertEquals(d(1, 22), calendar.intToDate(5))
    }
}