import java.time.DayOfWeek
import java.time.LocalDate

interface ICalendar {
    fun intToDate(int: Int): LocalDate
    fun dateToInt(date: LocalDate): Int
}

fun isWeekend(date: LocalDate): Boolean =
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

class Calendar(val startingDate: LocalDate, val holidayChecker: (LocalDate) -> Boolean) : ICalendar {

    val intMap: MutableMap<Int, LocalDate> = hashMapOf(0 to startingDate)
    val dateMap: MutableMap<LocalDate, Int> = hashMapOf(startingDate to 0)

    var lastInt = 0
    var lastDate = startingDate

    private fun fillUntil(condition: Pair<Int, LocalDate>.() -> Boolean) {
        lastInt++
        do {
            lastDate = lastDate.plusDays(1)
        } while (holidayChecker(lastDate))
    }

    override fun intToDate(int: Int): LocalDate {
        if (int < 0)
            throw AssertionError()

        if (int <= lastInt)
            return intMap.getValue(int)

        return lastDate
    }

    override fun dateToInt(date: LocalDate): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class Project(private val calendar: Calendar) : ICalendar by calendar {
    private val tasks: MutableList<Task> = mutableListOf()
    private val developers: MutableList<Developer> = mutableListOf()

    fun addFullStackTask(name: String, backCost: Int, frontCost: Double) {
        tasks.add(Task("$name (B)", TaskType.BACK_END, backCost.toDouble()))
        tasks.add(Task("$name (F)", TaskType.FRONT_END, frontCost))
    }

    fun addFullStackTask(name: String, backCost: Int, frontCost: Int) {
        addFullStackTask(name, backCost, frontCost.toDouble())
    }

    fun addDeveloper(developer: Developer) {
        developers.add(developer)
    }
}