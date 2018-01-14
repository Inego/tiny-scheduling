package inego.tinyscheduling

data class TaskAssignment(val date: Int, val developer: Developer)

const val DISTRACTED_PENALTY = 0.1
const val EARLY_START_PENALTY = 5
const val UNSATISFIED_DEPENDENCY_PENALTY = 5
const val UNFINISHED_TASK_PENALTY = 7

data class SolutionScore(val length: Int, val penalty: Double)

class Solution(private val project: Project) {
    private val assignments: MutableMap<Task, TaskAssignment> = hashMapOf()

    fun assign(task: Task, date: Int, developer: Developer) {
        if (task.type != developer.type)
            throw AssertionError()
        assignments.put(task, TaskAssignment(date, developer))
    }

    fun computeCost(): SolutionScore {

        val unassigned = assignments.keys.toMutableSet()

        val accumulated: MutableMap<Task, Double> = createInitialAccumulated()

        val developerTasks: MutableMap<Developer, Task> = mutableMapOf()

        val byDates = arrangeAssignmentsByDates()

        val completedTasks: MutableSet<Task> = mutableSetOf()

        var penalty = 0.0
        var currentDate = 0

        val finishedToday: MutableList<Pair<Task, Developer>> = mutableListOf()

        do {
            // Assign tasks which are scheduled beginning from this day
            for ((task, developer) in byDates.getOrDefault(currentDate, emptyList())) {
                unassigned.remove(task)
                developerTasks.put(developer, task)
            }

            finishedToday.clear()

            for ((developer, task) in developerTasks.entries) {
                // Subtract from the developer's efficiency all penalties coming from his subordinates
                // who are occupied with their own tasks
                val distractors = developerTasks.keys.count { it.leader == developer }
                val developerEfficiency = developer.efficiency - distractors * DISTRACTED_PENALTY

                val currentAccumulated = accumulated.getValue(task) + developerEfficiency

                accumulated.put(task, currentAccumulated)

                if (currentAccumulated >= task.cost) {
                    finishedToday.add(Pair(task, developer))
                }

                // Developer starting date penalty
                developer.startingDate?.let {
                    if (it > currentDate) {
                        penalty += EARLY_START_PENALTY
                    }
                }

                // Task dependency penalty
                task.dependsOn?.let {
                    if (it !in completedTasks) {
                        penalty += UNSATISFIED_DEPENDENCY_PENALTY
                    }
                }
            }

            for ((task, developer) in finishedToday) {
                developerTasks.remove(developer)
            }

            currentDate++

        } while (!(developerTasks.isEmpty() && unassigned.isEmpty()))

        for (task in project.tasks) {
            val completed = accumulated.getOrDefault(task, 0.0)
            if (completed < task.cost) {
                penalty += UNFINISHED_TASK_PENALTY * (task.cost - completed)
            }
        }

        return SolutionScore(currentDate, penalty)
    }

    private fun arrangeAssignmentsByDates(): Map<Int, List<Pair<Task, Developer>>> {

        return assignments.entries.groupBy(
                { it.value.date },
                { Pair(it.key, it.value.developer) }
        )
    }

    fun createInitialAccumulated(): HashMap<Task, Double> {
        return HashMap<Task, Double>(assignments.size).apply {
            assignments.keys.forEach { put(it, 0.0) }
        }
    }
}
