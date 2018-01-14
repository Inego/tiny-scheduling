package inego.tinyscheduling

import java.util.*

const val MUTATION_FACTOR = 7

class Population(val project: Project, val individuals: List<Solution>) {

}

fun Project.createPopulation(individuals: List<Solution>): Population = Population(this, individuals)


fun createChildFromParents(parent1: Solution, parent2: Solution): Solution {
    val project = parent1.project

    val child = Solution(project)

    for (task in project.tasks) {
        val assignment1 = parent1.getAssignment(task)
        val assignment2 = parent2.getAssignment(task)

        val childAssignment: TaskAssignment = when {
            assignment1 == assignment2 -> project.probabilisticMutation(assignment1)
            Random().nextInt(2) == 0 -> assignment1
            else -> {
                assignment2
            }
        }
        child.assign(task, childAssignment)
    }

    return child
}

fun Project.probabilisticMutation(assignment: TaskAssignment): TaskAssignment {
    return if (Random().nextInt(MUTATION_FACTOR) == 0) {
        mutate(assignment)
    }
    else assignment
}

private fun Project.mutate(taskAssignment: TaskAssignment): TaskAssignment {
    TODO()
}
