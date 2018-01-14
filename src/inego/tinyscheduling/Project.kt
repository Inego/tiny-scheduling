package inego.tinyscheduling

import java.util.*
import kotlin.math.ceil

class Project(private val calendar: Calendar) : ICalendar by calendar {
    val tasks: MutableList<Task> = mutableListOf()
    val developers: MutableList<Developer> = mutableListOf()

    private val maxEstimatedLength: Int by lazy {
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
}