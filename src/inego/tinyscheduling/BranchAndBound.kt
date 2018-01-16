package inego.tinyscheduling

import java.lang.Long.max
import kotlin.math.max


typealias BranchAndBoundSolution = List<BranchAndBoundAssignment>

class BranchAndBound(val project: Project) {
    var best = Int.MAX_VALUE
    var bestSolution: BranchAndBoundSolution? = null
}

class BranchAndBoundAssignment(
        val task: Task,
        val developer: Developer,
        val start: Int,
        val end: Int
) {
    override fun toString(): String = "$task to $developer ($start - $end)"
}

fun branchAndBoundRecursion(
        bb: BranchAndBound,
        leftTasks: Set<Task>,
        tasks: Map<Task, Int>,
        devs: Map<Developer, Int>,
        currentSolution: BranchAndBoundSolution
) {

    // Compose a list of possible assignments

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

            val end: Int = start + (task.cost.toDouble() * 8 / developer.efficiency).toInt()

            if (end < bb.best) {
                assignments.add(BranchAndBoundAssignment(task, developer, start, end))
            }
        }
    }

    if (assignments.isEmpty()) {
        return
    }

//    assignments.sortBy { it.end }



    // Iterate recursively

    if (leftTasks.size == 1) {
        // Just take the first assignment, it is locally best

        //assignments.shuffle()
        assignments.sortBy { it.end }

        val firstAssignment = assignments.first()

        bb.best = firstAssignment.end
        bb.bestSolution = currentSolution + firstAssignment

        println(bb.best)

    }
    else {
        assignments.shuffle()
        for (assignment in assignments) {
            val end = assignment.end
            if (end >= bb.best)
                continue
//                break

            val task = assignment.task

            // Copy all collections to be used downstream

            val newTasks = tasks.toMutableMap()
            newTasks.put(task, end)

            val newDevs = devs.toMutableMap()
            newDevs.put(assignment.developer, end)

            branchAndBoundRecursion(
                    bb,
                    leftTasks - task,
                    newTasks,
                    newDevs,
                    currentSolution + assignment
            )
        }
    }





}

fun useBranchAndBound(p: Project) {

    val bb = BranchAndBound(p)

    val tasks = mapOf<Task, Int>()

    // Prepare developer availability times
    val devs: MutableMap<Developer, Int> = mutableMapOf()

    for (developer in p.developers) {
        devs.put(developer, if (developer.startingDate == null) 0 else developer.startingDate * 8)
    }

    val leftTasks = p.tasks.toSet()
    branchAndBoundRecursion(bb, leftTasks, tasks, devs, emptyList())

}