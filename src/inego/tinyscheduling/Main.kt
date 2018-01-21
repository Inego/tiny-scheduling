package inego.tinyscheduling

import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max

val startingDate = LocalDate.of(2018, 1, 17)!!

fun main(args: Array<String>) {

    val calendar = Calendar(startingDate, ::isWeekend)

    val p = Project(calendar)

    val yanis = Developer("Yanis", TaskType.BACK_END)
    val dima = Developer("Dima", TaskType.FRONT_END, efficiency = 0.8)

    p.addDeveloper(yanis)
    p.addDeveloper(Developer("Alex", TaskType.BACK_END, 0.8, leader = yanis, startingDate = 6))
    p.addDeveloper(Developer(
            "Misha",
            TaskType.BACK_END,
            0.7,
            startingDate = calendar.dateToInt(LocalDate.of(2018, 2, 1)),
            leader = yanis
    ))

    p.addDeveloper(dima)
    p.addDeveloper(Developer("Sveta", TaskType.FRONT_END, efficiency = 0.8))
    p.addDeveloper(Developer("Max", TaskType.FRONT_END, efficiency = 0.7))

    p.addFullStackTask("Admin panel", 9, 6)
    p.addFullStackTask("Reports", 5, 3)
    p.addFullStackTask("Catalog", 5, 5)
    p.addFullStackTask("Dealers", 4, 4)
    p.addFullStackTask("News", 2, 1.5)
    p.addFullStackTask("Employees", 3, 3)
    p.addFullStackTask("Partners", 6, 4)
    p.addFullStackTask("Marketing", 3, 3)

//    useGenetic(p, calendar)
//    useMCTS(p)

//    useBranchAndBound(p)
    useMctsBranchAndBound(p)
}




fun randomWithMCTS(p: Project, maxIter: Int): Solution {

    var tree = Tree(p)

    var currentBest = p.createRandomSolution()
    var nextBest = currentBest

    var bestCost = currentBest.cost.total

    var currentMaxIter = maxIter

    var counter = 0

    while (true) {
        if (counter > currentMaxIter) {

//            println("$currentMaxIter $bestCost")

            if (nextBest == currentBest)
            {
                return currentBest
            }

            currentBest = nextBest
            tree = Tree(p)
            counter = 0
//            currentMaxIter += maxIter
        }
        val solution = tree.playout(currentBest, bestCost, 2)

        val solutionScore = solution.cost.total

        if (solutionScore < bestCost) {
            bestCost = solutionScore
            nextBest = solution
        }

        counter++

    }

}


private fun useMCTS(p: Project) {
    var tree = Tree(p)

    var currentBest = p.createRandomSolution()

    var best = currentBest.cost.total

    var counter = 0

    while (true) {
        if (counter % 10000 == 0) {
            println(counter)
        }
        val solution = tree.playout(currentBest, best, 2)

        val solutionScore = solution.cost.total

        if (solutionScore < best) {
            best = solutionScore
            println(solution)
            println(best)
            currentBest = solution
            tree = Tree(p)
            counter = 0
        } else {

            counter++
        }
    }
}

private fun useGenetic(p: Project, calendar: Calendar) {

    var bestest = p.createRandomSolution()
    var bestestScore = bestest.cost.total

    var topCounter = 0

    while (true) {

        topCounter++

        println(topCounter)

        //var pop = p.createRandomPopulation(50)
        var pop = p.createRandomPopulationByMCTS(50, 1000)

        var bestWeight = Double.MAX_VALUE
        var bestSolution: Solution?

        var genCounter = 0
        var maxGens = 10000

        while (genCounter < maxGens) {

            if (pop.estimate.minWeight < bestWeight) {
                bestWeight = pop.estimate.minWeight
                bestSolution = pop.estimate.best


                maxGens = max(maxGens, genCounter * 3)

                genCounter = 0

                if (bestWeight < bestestScore) {
                    bestest = bestSolution!!
                    bestestScore = bestWeight

                    println(bestest.toString(calendar))
                    println("${calendar.intToDate(ceil(bestestScore).toInt())} ($bestestScore)")
                }
            }

            pop = pop.createNextGeneration()

            genCounter++
        }

    }




//    var pop = p.createRandomPopulation(50)

//    var pop = p.createRandomPopulationByMCTS(50, 1000)


}

private fun Project.createRandomPopulationByMCTS(size: Int, maxIter: Int): Population {
    val individuals: MutableList<Solution> = mutableListOf()

    for (i in 0..size) {
        println("Creating initial $i...")
        individuals.add(randomWithMCTS(this, maxIter))
    }
    return createPopulation(individuals)
}
