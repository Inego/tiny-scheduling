package inego.tinyscheduling

import java.util.*
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

class BbNode(val parent: BbNode?, val aggScore: Int) {
    var playouts = if (parent == null) 1 else 0
    var best = Int.MAX_VALUE
    var worst: Int = 0
    var children = mutableMapOf<BranchAndBoundAssignment, BbNode>()
    var untriedChildren: LinkedList<BranchAndBoundAssignment>? = null
    fun newChild(assignment: BranchAndBoundAssignment) = BbNode(
            this,
            max(aggScore, assignment.end)
    ).also { children[assignment] = it }

    override fun toString() = "[$playouts] $best -- $worst"
}

class BbTree(private val project: Project) {
    private val root = BbNode(null, 0)

    private var best = Int.MAX_VALUE

    fun playout() {

        val leftTasks: MutableSet<Task> = project.tasks.toMutableSet()
        val tasks: MutableMap<Task, Int> = mutableMapOf()
        val devs = project.developers.associate { Pair(it, it.startingDate * 8) }.toMutableMap()

        var currentNode = root

        val currentSolution: MutableList<BranchAndBoundAssignment> = mutableListOf()

        var currentScore = 0

        var random = false

        fun getPossibleAssignments(): LinkedList<BranchAndBoundAssignment> {
            val result = LinkedList<BranchAndBoundAssignment>()
            for (task in leftTasks) {
                val parentTask = task.dependsOn
                if (parentTask != null && parentTask !in tasks) continue


                val possibleDevs = if (task.onlyBy != null) listOf(task.onlyBy)
                else project.devsByType.getValue(task.type)

                for (developer in possibleDevs) {
                    var start = devs.getValue(developer)

                    if (parentTask != null) {
                        start = max(start, tasks.getValue(parentTask))
                    }

                    val end: Int = start + (task.cost * 8 / developer.efficiency).toInt()
                    if (end < best)
                        result.add(BranchAndBoundAssignment(task, developer, start, end))
                }
            }
            return result
        }

        fun BranchAndBoundAssignment.add() {
            currentScore = max(currentScore, end)

            currentSolution.add(this)
            leftTasks.remove(task)
            tasks[task] = end
            devs[developer] = end
        }

        while (leftTasks.isNotEmpty()) {

            if (random) {
                val nextMoves = getPossibleAssignments()  // May be empty in case of bound shortcut
                if (nextMoves.isEmpty()) {
                    currentScore = currentNode.parent!!.worst + 1
                    break
                }
                nextMoves.getRandomElement().add()

            } else {
                if (currentNode.playouts == 1) {
                    // Explode the node --- but note that the incoming children may be empty because of bound shortcuts
                    currentNode.untriedChildren = getPossibleAssignments().apply { shuffle() }
                }

                val untriedChildren = currentNode.untriedChildren

                if (untriedChildren != null) {
                    // There are assignments at this node which have not yet been tried.
                    // Get another one and transform it into a regular node.

                    var assignment: BranchAndBoundAssignment? = null

                    while (untriedChildren.isNotEmpty()) {
                        assignment = untriedChildren.pop()
                        if (assignment.end >= best) {
                            assignment = null
                        } else {
                            break
                        }
                    }

                    if (untriedChildren.isEmpty()) {
                        currentNode.untriedChildren = null
                    }

                    if (assignment != null) {
                        currentNode = currentNode.newChild(assignment)
                        assignment.add()
                        random = true
                        continue
                    }
                }

                // All children of this node have been played at least once.
                // This means we can select from them using the MCTS formula.

                // TODO consider handling the case with the only child

                var bestEntry: MutableMap.MutableEntry<BranchAndBoundAssignment, BbNode>? = null
                var bestScore = -1.0

                val worst = currentNode.worst

                val denominator = (worst - best).toDouble()
                val lnNodePlays = ln(currentNode.playouts.toDouble())

                for (entry in currentNode.children.entries) {

                    val node = entry.value
                    if (node.aggScore >= best) continue   // BB shortcut

                    val childScore = (worst - node.best) / denominator + c * sqrt(lnNodePlays / node.playouts)

                    if (bestScore < childScore) {
                        bestEntry = entry
                        bestScore = childScore
                    }
                }

                if (bestEntry == null) {

                    currentScore = currentNode.worst + 1

                    // ALL children of this node became uninteresting.
                    // Destruct it (possibly destructing the nodes upstream)

                    for (i in currentSolution.size - 1 downTo 0) {
                        val parentNode = currentNode.parent!!
                        val parentChildren = parentNode.children
                        parentChildren.remove(currentSolution[i])
                        currentNode = parentNode
                        if (parentChildren.isNotEmpty()) {
                            break
                        }
                    }

                    break

                } else bestEntry.run {
                    key.add()
                    currentNode = value
                }
            }
        }

        // Back-propagate playout results

        var backPropNode: BbNode? = currentNode

        while (backPropNode != null) {
            backPropNode.playouts++
            if (backPropNode.worst < currentScore)
                backPropNode.worst = currentScore
            if (backPropNode.best > currentScore)
                backPropNode.best = currentScore
            backPropNode = backPropNode.parent
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

    var counter = 0

    while (true) {
        tree.playout()
        counter++
        if (counter % 10000 == 0) {
            println(counter)
        }
    }
}