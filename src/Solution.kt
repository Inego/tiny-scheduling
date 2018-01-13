data class TaskAssignment(val date: Int, val developer: Developer)

class Solution(val project: Project) {
    val assignments: MutableMap<Task, TaskAssignment> = hashMapOf()

    fun computeCost(): Double {
        return 0.0
    }
}