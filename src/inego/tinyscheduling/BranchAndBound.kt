package inego.tinyscheduling

import kotlin.math.max


typealias BranchAndBoundSolution = List<BranchAndBoundAssignment>

class BranchAndBound(val project: Project) {
    var best = Int.MAX_VALUE
    var bestSolution: BranchAndBoundSolution? = null
    var counter = 0
}

class BranchAndBoundAssignment(
        val task: Task,
        val developer: Developer,
        private val start: Int,
        val end: Int
) {
    override fun toString(): String = "$task to $developer ($start - $end)"
    fun toString(calendar: Calendar): String = "$task to $developer (${calendar.hoursToString(start)} - ${calendar.hoursToString(end)})"
}

fun branchAndBoundRecursion(
        bb: BranchAndBound,
        leftTasks: Set<Task>,
        tasks: Map<Task, Int>,
        devs: Map<Developer, Int>,
        currentSolution: BranchAndBoundSolution,
        solutionEnd: Int
) {
    bb.counter++
//    if (bb.counter % 100000 == 0) {
//        println(bb.counter)
//    }

    currentSolution
            .filter { it.end > solutionEnd }
            .forEach { throw AssertionError() }

    // Compose a list of possible assignments

    if (solutionEnd >= bb.best)
        return

    val assignments: MutableList<BranchAndBoundAssignment> = mutableListOf()

    for (task in leftTasks) {
        if (task.dependsOn != null && task.dependsOn !in tasks) {
            continue
        }
        for (developer in bb.project.devsByType.getValue(task.type)) {

            var start = devs.getValue(developer)

            if (task.dependsOn != null) {
                start = max(start, tasks.getValue(task.dependsOn))
            }

            val end: Int = start + (task.cost * 8 / developer.efficiency).toInt()

            if (end < bb.best) {
                assignments.add(BranchAndBoundAssignment(task, developer, start, end))
            }
        }
    }

    if (assignments.isEmpty()) {
        return
    }

    assignments.sortBy { it.end }

    // Iterate recursively

    if (leftTasks.size == 1) {
        // Just take the first assignment, it is locally best

//        assignments.sortBy { it.end }

        val firstAssignment = assignments.first()

        val end = max(solutionEnd, firstAssignment.end)

        if (end < bb.best) {
            bb.best = end
            bb.bestSolution = currentSolution + firstAssignment

            println(bb.best)
            printBbSolution(bb.bestSolution!!, bb.project.calendar)
        }
    } else {
//        assignments.shuffle()
        for (assignment in assignments) {

            val end = assignment.end

            if (solutionEnd == 0) {
                println(assignment)
            }

            if (end >= bb.best)
//                continue
                break

            val task = assignment.task

            // Copy all collections to be used downstream

            val newTasks = tasks.toMutableMap()
            newTasks[task] = end

            val newDevs = devs.toMutableMap()
            newDevs[assignment.developer] = end

            branchAndBoundRecursion(
                    bb,
                    leftTasks - task,
                    newTasks,
                    newDevs,
                    currentSolution + assignment,
                    max(solutionEnd, assignment.end)
            )
        }
    }
}

fun printBbSolution(solution: BranchAndBoundSolution, calendar: Calendar) {

    for (assignment in solution) {
        println(assignment.toString(calendar))
    }

}


fun useBranchAndBound(p: Project, initial: Int = Int.MAX_VALUE) {

    val bb = BranchAndBound(p)
    bb.best = initial

    val tasks = mapOf<Task, Int>()

    // Prepare developer availability times
    val devs: MutableMap<Developer, Int> = mutableMapOf()
    for (developer in p.developers) {
        devs[developer] = developer.startingDate * 8
    }

    val leftTasks = p.tasks.toSet()
    branchAndBoundRecursion(bb, leftTasks, tasks, devs, emptyList(), 0)

}