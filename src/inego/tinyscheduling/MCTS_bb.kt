package inego.tinyscheduling

import kotlin.math.max

class BbNode(val parent: BbNode?) {
    var children: LinkedHashMap<BranchAndBoundAssignment, BbNode?>? = null
}

class BbTree(private val project: Project) {
    private val root = BbNode(null)

    private var best = Int.MAX_VALUE

    fun playout() {

        val leftTasks: MutableSet<Task> = project.tasks.toMutableSet()
        val tasks: MutableMap<Task, Int> = mutableMapOf()
        val devs: MutableMap<Developer, Int> = mutableMapOf()

        for (developer in project.developers) {
            devs[developer] = if (developer.startingDate == null) 0 else developer.startingDate * 8
        }

        var currentNode = root

        val currentSolution: MutableList<BranchAndBoundAssignment> = mutableListOf()

        var currentScore = 0

        while (leftTasks.isNotEmpty()) {

            if (currentNode.children == null) {

                // Explode

                val children = LinkedHashMap<BranchAndBoundAssignment, BbNode?>()
                currentNode.children = children

                for (task in leftTasks) {
                    if (task.dependsOn != null && task.dependsOn !in tasks) {
                        continue
                    }
                    for (developer in project.devsByType.getValue(task.type)) {

                        var start = devs.getValue(developer)

                        if (task.dependsOn != null) {
                            start = max(start, tasks.getValue(task.dependsOn))
                        }

                        val end: Int = start + (task.cost * 8 / developer.efficiency).toInt()

                        if (end < best) {
                            children[BranchAndBoundAssignment(task, developer, start, end)] = null
                        }
                    }
                }
            }

            val children = currentNode.children!!

            val choices: MutableList<BranchAndBoundAssignment> = mutableListOf()

            val toRemove: MutableList<BranchAndBoundAssignment> = mutableListOf()

            for (assignment in children.keys) {
                if (assignment.end >= best) {
                    toRemove.add(assignment)
                }
                else {
                    choices.add(assignment)
                }
            }

            for (assignmentToRemove in toRemove) {
                children.remove(assignmentToRemove)
            }

            if (choices.isEmpty()) {
                for (i in currentSolution.size - 1 downTo 0) {
                    if (currentNode.parent == null) {
                        break
                    }
                    currentNode = currentNode.parent!!
                    val assignment = currentSolution[i]
                    val ch = currentNode.children!!
                    ch.remove(assignment)

                    if (ch.isNotEmpty()) {
                        break
                    }

                }

                return
            }
            else {
                val childAssignment = choices.getRandomElement()

                var node = children.getValue(childAssignment)

                currentScore = max(currentScore, childAssignment.end)

                if (node == null) {
                    node = BbNode(currentNode)
                    children[childAssignment] = node
                }

                currentNode = node
                currentSolution.add(childAssignment)

                leftTasks.remove(childAssignment.task)
                tasks[childAssignment.task] = childAssignment.end
                devs[childAssignment.developer] = childAssignment.end
            }
        }

        if (currentScore < best) {
            best = currentScore
            println("$best: ${project.calendar.hoursToString(best)}")
            printBbSolution(currentSolution, project.calendar)
        }
    }
}


fun useMctsBranchAndBound(p: Project) {

    val tree = BbTree(p)
//    tree.best = 170

    while (true) {
        tree.playout()
    }

}