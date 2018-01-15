package inego.tinyscheduling

class Task(
        val name: String,
        val type: TaskType,
        val cost: Double,
        val dependsOn: Task? = null
) {
    constructor(name: String, type: TaskType, cost: Int, dependsOn: Task? = null)
            : this(name, type, cost.toDouble(), dependsOn = dependsOn)

    override fun toString() = name
}