package inego.tinyscheduling

import java.util.Comparator
import java.util.concurrent.ThreadLocalRandom

const val MUTATION_FACTOR = 3
const val MAX_DATE_MUTATION = 5
const val DEATH_THRESHOLD = 0.5

val rnd = ThreadLocalRandom.current()!!


class ComputationEstimate(population: Population) {
    var weights: MutableMap<Solution, Double> = mutableMapOf()

    var minWeight = Double.MAX_VALUE
    var maxWeight = 0.0

    var best: Solution? = null

    init {

        for (solution in population.individuals) {
            val weight = solution.cost.total

            if (weight < minWeight) {
                minWeight = weight
                best = solution
            }

            if (weight > maxWeight)
                maxWeight = weight

            weights[solution] = weight
        }
    }

}


class Population(private val project: Project, val individuals: List<Solution>) {

    val estimate: ComputationEstimate by lazy {
        ComputationEstimate(this)
    }

    fun createNextGeneration(): Population {

        val maxWeight = estimate.maxWeight
        val minWeight = estimate.minWeight
        val weights = estimate.weights


        // TODO support the case when all solutions are equal (minWeight == maxWeight)

        // Second pass. Fitness = (max - weight) / (max - min),
        // so that the fittest will have 1.0 while the least fittest will have 0.0 (and thus omitted from the set
        // of the potential parents)

        val denominator = maxWeight - minWeight

        val parents: MutableMap<Solution, Double> = mutableMapOf()

        for ((solution, weight) in weights.entries) {
            val fitness = (maxWeight - weight) / denominator
            if (fitness > DEATH_THRESHOLD)
                parents[solution] = fitness
        }

        // Now sort the parents reversely
        val sortedParents = parents.toSortedMap(Comparator { o1, o2 ->
            parents.getValue(o2).compareTo(parents.getValue(o1))
        })

        // Aggregate weights by running totals
        // TODO check if there's an idiomatic way to do this using the std library
        var fitnessSum = 0.0
        val sortedWeightedParents: MutableMap<Solution, Double> = LinkedHashMap(sortedParents.size)
        for ((solution, fitness) in sortedParents.entries) {
            fitnessSum += fitness
            sortedWeightedParents[solution] = fitnessSum
        }

        val result: MutableList<Solution> = mutableListOf(estimate.best!!)
//        val result: MutableList<Solution> = mutableListOf()

        fun pickParent(): Solution {
            val point = rnd.nextDouble(fitnessSum)
            var picked = sortedWeightedParents.keys.first()
            for ((solution, aggFitness) in sortedWeightedParents.entries) {
                if (aggFitness > point) {
                    break
                }
                picked = solution
            }
            return picked
        }

        while (result.size < individuals.size) {
            // Pick a random pair
            val parent1 = pickParent()
            val parent2 = pickParent()

            if (parent1 != parent2) {
                result.add(createChildFromParents(parent1, parent2))
            }
        }

//        // Add a bunch of fresh blood
//        repeat (10) {
//            val fresh = randomWithMCTS(project, 100)
//            result.add(fresh)
//        }

        return Population(project, result)
    }

}

fun Project.createPopulation(individuals: List<Solution>): Population = Population(this, individuals)


fun Project.createRandomPopulation(size: Int): Population {
    val individuals: List<Solution> = List(size) { createRandomSolution() }
    return createPopulation(individuals)
}


fun createChildFromParents(parent1: Solution, parent2: Solution): Solution {

    val project = parent1.project

    val child = Solution(project)

    for (task in project.tasks) {
        val assignment1 = parent1.getAssignment(task)
        val assignment2 = parent2.getAssignment(task)

        val childAssignment: TaskAssignment = when {
            assignment1 == assignment2 -> project.mutateMaybe(task, assignment1)
            tossCoin() -> assignment1
            else -> {
                assignment2
            }
        }
        child.assign(task, childAssignment)
    }

    return child
}


fun tossCoin() = rnd.nextInt(2) == 0


/**
 * Returns the specified task assignment, possibly mutated
 */
private fun Project.mutateMaybe(task: Task, taskAssignment: TaskAssignment): TaskAssignment {

    // Leave the assignment intact for most cases
    if (rnd.nextInt(MUTATION_FACTOR) > 0) {
        return taskAssignment
    }

    // Perform actual mutation
    val outcome = rnd.nextInt(5)

    var (date, developer) = taskAssignment

    // 0, 1 -> Change date
    // 2    -> Change date & dev
    // 3, 4 -> Change dev

    if (outcome < 3) {
        // Change date

        val dir = when {
            date <= 0 -> 1
            date >= maxEstimatedLength -> -1
            else -> if (tossCoin()) 1 else -1
        }

        date += dir * (1 + rnd.nextInt(MAX_DATE_MUTATION))

        if (date < 0)
            date = 0
    }
    if (outcome > 1) {
        // Change to a different dev of the same type
        val type = developer.type

        // TODO this may crash if the current developer is the only one of his type
        developer = devsByType.getValue(type).filter { it != developer }.getRandomElement()

    }

    if (task.type != developer.type) {
        throw AssertionError()
    }

    return TaskAssignment(date, developer)
}
