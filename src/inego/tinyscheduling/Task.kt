package inego.tinyscheduling

class Task(
        private val name: String,
        val type: TaskType,
        val cost: Double,
        val dependsOn: Task? = null,
        val onlyBy: Developer? = null
) {
    constructor(name: String, type: TaskType, cost: Int, dependsOn: Task? = null, onlyBy: Developer? = null)
            : this(name, type, cost.toDouble(), dependsOn = dependsOn, onlyBy = onlyBy)

    override fun toString() = name
}