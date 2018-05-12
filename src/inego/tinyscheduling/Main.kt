package inego.tinyscheduling

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlin.math.ceil
import kotlin.math.max

const val MODE_MCTS_BB: String = "mcts_bb"
const val MODE_MCTS: String = "mcts"
const val MODE_BB: String = "bb"
const val MODE_GENETIC: String = "genetic"

private class ProgramArgs(parser: ArgParser) {
    val mode by parser.positional("Mode of computation").default(MODE_MCTS_BB)
}

fun main(args: Array<String>) {

    val parsedArgs = ArgParser(args).parseInto(::ProgramArgs)

    val p = createSampleProject()

    val mode = parsedArgs.mode

    when (mode) {
        MODE_MCTS_BB -> useMctsBranchAndBound(p)
        MODE_MCTS -> useMCTS(p)
        MODE_BB -> useBranchAndBound(p)
        MODE_GENETIC -> useGenetic(p, createSampleCalendar())
        else -> throw IllegalArgumentException("Unknown mode: $mode")
    }
}

fun randomWithMCTS(p: Project, maxIter: Int): Solution {

    var tree = Tree(p)

    var currentBest = p.createRandomSolution()
    var nextBest = currentBest

    var bestCost = currentBest.cost.total

    var counter = 0

    while (true) {
        if (counter > maxIter) {

            if (nextBest == currentBest)
            {
                return currentBest
            }

            currentBest = nextBest
            tree = Tree(p)
            counter = 0

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

}

private fun Project.createRandomPopulationByMCTS(size: Int, maxIter: Int): Population {
    val individuals: MutableList<Solution> = mutableListOf()

    for (i in 0..size) {
        println("Creating initial $i...")
        individuals.add(randomWithMCTS(this, maxIter))
    }
    return createPopulation(individuals)
}
