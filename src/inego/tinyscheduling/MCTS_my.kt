package inego.tinyscheduling

import java.util.Objects.hash
import kotlin.math.ln
import kotlin.math.sqrt

val c = sqrt(2.0)

abstract class Modification(val task: Task) {
    abstract fun applyTo(solution: Solution)
}

class ChangeDeveloper(tsk: Task, private val developer: Developer) : Modification(tsk) {
    override fun applyTo(solution: Solution) {
        val current = solution.assignments.getValue(task)
        solution.assign(task, TaskAssignment(current.date, developer))
    }

    override fun hashCode(): Int {
        return hash(task, developer)
    }

    override fun equals(other: Any?): Boolean {
        if (other is ChangeDeveloper) {
            return other.task == task && other.developer == developer
        }
        return false
    }
}

class ChangeDate(tsk: Task, private val date: Int) : Modification(tsk) {
    override fun applyTo(solution: Solution) {
        val current = solution.assignments.getValue(task)
        solution.assign(task, TaskAssignment(date, current.developer))
    }

    override fun hashCode(): Int {
        return hash(task, date)
    }

    override fun equals(other: Any?): Boolean {
        if (other is ChangeDate) {
            return other.task == task && other.date == date
        }
        return false
    }
}


class Node(val parent: Node?) {
    var children: LinkedHashMap<Modification, Node?>? = null
    var playouts = 0
    var sum = 0.0
    var avgWeight = 0.0
    fun explode() {
        children = LinkedHashMap()
    }
}

class Tree(private val project: Project) {
    private val root = Node(null)

    fun playout(baseSolution: Solution, treeBest: Double, maxDepth: Int): Solution {

        var currentNode = root
        var random = false

        val solution = baseSolution.clone()

        var randomCounter = 0

        while (!random || randomCounter < maxDepth) {

            if (random) {
                project.randomModification(solution).applyTo(solution)
                randomCounter++
            }
            else {
                if (currentNode.children == null) {
                    currentNode.explode()
                    random = true
                }

                val modification: Modification = project.randomModification(solution)

                val children = currentNode.children!!

                if (modification in children) {

                    var minWeight = treeBest
                    var maxWeight = 0.0
                    // Compute weights
                    for (entry in children) {
                        if (entry.value == null) {
                            throw AssertionError()
                        }
                        val weight = entry.value!!.avgWeight
                        if (weight > maxWeight) {
                            maxWeight = weight
                        }
                        if (weight < minWeight) {
                            minWeight = weight
                        }
                    }

                    val denominator = maxWeight - minWeight

                    var bestEntry = children.iterator().next()

                    var bestScore = 0.0

                    for (entry in children) {

                        val entryNode = entry.value!!

                        val weight = entryNode.avgWeight
                        val childPlayouts = entryNode.playouts

                        var score = if (denominator == 0.0) 1.0 else (maxWeight - weight) / denominator

                        score = score / childPlayouts + c * sqrt(ln(currentNode.playouts.toDouble()) / childPlayouts)

                        if (bestScore < score) {
                            bestScore = score
                            bestEntry = entry
                        }
                    }

                    bestEntry.key.applyTo(solution)
                    currentNode = bestEntry.value!!

                }
                else {
                    // Just use the random one
                    modification.applyTo(solution)

                    val childNode = Node(currentNode)
                    currentNode.children!![modification] = childNode

                    currentNode = childNode

                    random = true

                }
            }
        }

        val score = solution.cost.total

        while (true)
        {
            currentNode.sum += score

            currentNode.playouts++
            currentNode.avgWeight = currentNode.sum / currentNode.playouts

            if (currentNode.parent != null) currentNode = currentNode.parent!!
            else
                break
        }

        return solution
    }
}