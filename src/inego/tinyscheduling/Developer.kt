package inego.tinyscheduling

class Developer(
        private val name: String,
        val type: TaskType,
        val efficiency: Double = 1.0,
        val leader: Developer? = null,
        val startingDate: Int? = null
) {
    override fun toString(): String {
        return name
    }
}