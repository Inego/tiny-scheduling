package inego.tinyscheduling

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

interface ICalendar {
    fun intToDate(int: Int): LocalDate
    fun dateToInt(date: LocalDate): Int
}

fun isWeekend(date: LocalDate): Boolean {
    val dayOfWeek = date.dayOfWeek
    return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            || date.month == Month.FEBRUARY && date.dayOfMonth == 23
}

/**
 * A thread-unsafe implementation of [ICalendar].
 */
class Calendar(private val startingDate: LocalDate, private val holidayChecker: (LocalDate) -> Boolean) : ICalendar {

    private val intMap: MutableMap<Int, LocalDate> = hashMapOf(0 to startingDate)
    private val dateMap: MutableMap<LocalDate, Int> = hashMapOf(startingDate to 0)

    private var lastInt = 0
    private var lastDate = startingDate

    private fun fillUntil(condition: () -> Boolean) {
        do {
            lastInt++
            do {
                lastDate = lastDate.plusDays(1)
            } while (holidayChecker(lastDate))
            intMap[lastInt] = lastDate
            dateMap[lastDate] = lastInt
        } while (!condition())
    }

    override fun intToDate(int: Int): LocalDate {
        if (int < 0)
            throw AssertionError()

        if (int <= lastInt)
            return intMap.getValue(int)

        fillUntil { lastInt == int }

        return lastDate
    }

    override fun dateToInt(date: LocalDate): Int {
        if (date < startingDate)
            throw AssertionError()

        if (date <= lastDate)
            return dateMap.getValue(date)

        if (holidayChecker(date))
            // Prevent infinite loop
            throw AssertionError()

        fillUntil { lastDate == date }

        return lastInt
    }

    fun hoursToString(hours: Int): String {
        val dateInt = hours / 8
        val remainder = hours - dateInt * 8
        val date = intToDate(dateInt)
        return if (remainder != 0) "$date .$remainder" else date.toString()
    }
}