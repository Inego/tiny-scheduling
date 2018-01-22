package inego.tinyscheduling

import java.util.*
import kotlin.math.ceil

class Project(val calendar: Calendar) : ICalendar by calendar {
    val tasks: MutableList<Task> = mutableListOf()
    val developers: MutableList<Developer> = mutableListOf()

    internal val maxEstimatedLength: Int by lazy {
        ceil(tasks.sumByDouble { it.cost }).toInt()
    }

    val devsByType: Map<TaskType, List<Developer>> by lazy {
        developers.groupBy { it.type }
    }

    fun addFullStackTask(
            name: String,
            backCost: Int,
            frontCost: Double,
            backOnlyBy: Developer? = null,
            frontOnlyBy: Developer? = null
    ) {
        val backendTask = Task(
                "$name (B)",
                TaskType.BACK_END,
                backCost.toDouble(),
                onlyBy = backOnlyBy
        )
        tasks.add(backendTask)
        tasks.add(Task(
                "$name (F)",
                TaskType.FRONT_END,
                frontCost,
                dependsOn = backendTask,
                onlyBy = frontOnlyBy
        ))
    }

    fun addFullStackTask(
            name: String,
            backCost: Int,
            frontCost: Int,
            backOnlyBy: Developer? = null,
            frontOnlyBy: Developer? = null
    ) {
        addFullStackTask(name, backCost, frontCost.toDouble(), backOnlyBy, frontOnlyBy)
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