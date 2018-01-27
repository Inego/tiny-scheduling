package inego.tinyscheduling

class Task(
        private val name: String,
        val type: TaskType,
        val cost: Double,
        val dependsOn: Task? = null,
        val onlyBy: Developer? = null,
        val first: Boolean = false
) {
    constructor(
            name: String,
            type: TaskType,
            cost: Int,
            dependsOn: Task? = null,
            onlyBy: Developer? = null,
            first: Boolean = false
    )
            : this(name, type, cost.toDouble(), dependsOn = dependsOn, onlyBy = onlyBy, first = first)

    override fun toString() = name
}