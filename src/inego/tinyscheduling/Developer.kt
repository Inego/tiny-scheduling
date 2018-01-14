package inego.tinyscheduling

class Developer(
        val name: String,
        val type: TaskType,
        val efficiency: Double = 1.0,
        val leader: Developer? = null,
        val startingDate: Int? = null
)