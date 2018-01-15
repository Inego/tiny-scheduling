package inego.tinyscheduling

import java.util.LinkedList
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class TreeNode {

    private val children: MutableList<TreeNode> = mutableListOf()
    private var nVisits: Double = 0.toDouble()
    private var totValue: Double = 0.toDouble()

    val isLeaf
        get() = children.isEmpty()

    fun selectAction() {
        val visited = LinkedList<TreeNode>()
        var cur: TreeNode = this
        visited.add(this)
        while (!cur.isLeaf) {
            cur = cur.select()
            visited.add(cur)
        }
        cur.expand()
        val newNode = cur.select()
        visited.add(newNode)
        val value = rollOut(newNode)
        for (node in visited) {
            // would need extra logic for n-player game
            node.updateStats(value)
        }
    }

    private fun expand() {
        for (i in 0 until nActions) {
            children[i] = TreeNode()
        }
    }

    private fun select(): TreeNode {
        var selected: TreeNode? = null
        var bestValue = java.lang.Double.MIN_VALUE
        for (c in children) {
            val uctValue = c.totValue / (c.nVisits + epsilon) +
                    Math.sqrt(Math.log(nVisits + 1) / (c.nVisits + epsilon)) +
                    r.nextDouble() * epsilon
            // small random number to break ties randomly in unexpanded nodes
            if (uctValue > bestValue) {
                selected = c
                bestValue = uctValue
            }
        }
        return selected!!
    }

    private fun rollOut(tn: TreeNode?): Double {
        // ultimately a roll out will end in some value
        // assume for now that it ends in a win or a loss
        // and just return this at random
        return r.nextInt(2).toDouble()
    }

    private fun updateStats(value: Double) {
        nVisits++
        totValue += value
    }

    fun arity(): Int {
        return children.size
    }

    companion object {
        internal var r = ThreadLocalRandom.current()
        internal var nActions = 5
        internal var epsilon = 1e-6
    }
}