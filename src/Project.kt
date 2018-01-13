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