package inego.tinyscheduling.tests

import inego.tinyscheduling.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SolutionTest {

    private lateinit var p: Project
    private lateinit var s: Solution

    @Before
    fun before() {
        p = Project(calendarFactory())
        s = Solution(p)
    }

    @Test
    fun `one day task, one dev`() {
        val dev = addDev()
        val t = addTask(1)
        s.assign(t, 0, dev)
        assertSolutionCost(1, 0.0)
    }

    @Test
    fun `penalty for an unhandled task`() {
        addTask(1)
        assertSolutionCost(1, UNFINISHED_TASK_PENALTY.toDouble())
    }

    @Test
    fun `two days task`() {
        val dev = addDev()
        val t = addTask(2)
        s.assign(t, 0, dev)
        assertSolutionCost(2, 0.0)
    }

    private fun addTask(cost: Int): Task {
        val task = Task("Test task ${p.tasks.size + 1}", TaskType.BACK_END, cost)
        return p.addTask(task)
    }

    private fun addDev(): Developer {
        val developer = Developer("Test dev ${p.developers.size + 1}", TaskType.BACK_END)
        return p.addDeveloper(developer)
    }

    private fun assertSolutionCost(assertedLength: Int, assertedPenalty: Double) {
        val (length, penalty) = s.cost
        assertEquals(assertedLength, length, "Incorrect solution length")
        assertEquals(assertedPenalty, penalty, "Incorrect solution penalty")
    }

}