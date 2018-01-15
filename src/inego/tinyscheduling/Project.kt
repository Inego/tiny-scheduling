package inego.tinyscheduling

import java.util.*
import kotlin.math.ceil

class Project(private val calendar: Calendar) : ICalendar by calendar {
    val tasks: MutableList<Task> = mutableListOf()
    val developers: MutableList<Developer> = mutableListOf()

    internal val maxEstimatedLength: Int by lazy {
        ceil(tasks.sumByDouble { it.cost }).toInt()
    }

    val devsByType: Map<TaskType, List<Developer>> by lazy {
        developers.groupBy { it.type }
    }

    fun addFullStackTask(name: String, backCost: Int, frontCost: Double) {
        val backendTask = Task("$name (B)", TaskType.BACK_END, backCost.toDouble())
        tasks.add(backendTask)
        tasks.add(Task("$name (F)", TaskType.FRONT_END, frontCost, dependsOn = backendTask))
    }

    fun addFullStackTask(name: String, backCost: Int, frontCost: Int) {
        addFullStackTask(name, backCost, frontCost.toDouble())
    }

    fun addDeveloper(developer: Developer): Developer {
        developers.add(developer)
        return developer
    }

    fun addTask(task: Task): Task {
        tasks.add(task)
        return task
    }

    fun createRandomSolution(): Solution {
        val result = Solution(this)

        for (task in tasks) {
            val developer = devsByType.getValue(task.type).getRandomElement()
            val date = Random().nextInt(maxEstimatedLength)
            result.assign(task, date, developer)
        }

        return result
    }

    val possibleTaskAssignments: Map<Task, List<TaskAssignment>> by lazy {
        val result = mutableMapOf<Task, List<TaskAssignment>>()

        for (task in tasks) {

            val list = mutableListOf<TaskAssignment>()

            for (developer in devsByType.getValue(task.type)) {
                (0..maxEstimatedLength).mapTo(list) { TaskAssignment(it, developer) }
            }

            result.put(task, list)
        }

        result
    }

    fun randomModification(source: Solution): Modification {

        val task = tasks.getRandomElement()

        var (date, developer) = source.assignments.getValue(task)

        if (tossCoin()) {
            // Change date

            val dir = when {
                date <= 0 -> 1
                date >= maxEstimatedLength -> -1
                else -> if (tossCoin()) 1 else -1
            }

            date += dir * (1 + rnd.nextInt(MAX_DATE_MUTATION))

            if (date < 0)
                date = 0

            return ChangeDate(task, date)

        }
        else {
            // Change to a different dev of the same type
            val type = developer.type

            // TODO this may crash if the current developer is the only one of his type
            developer = devsByType.getValue(type).filter { it != developer }.getRandomElement()
            return ChangeDeveloper(task, developer)
        }
    }
}