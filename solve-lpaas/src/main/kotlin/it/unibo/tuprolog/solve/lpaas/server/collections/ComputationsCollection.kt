package it.unibo.tuprolog.solve.lpaas.server.collections

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.util.idGenerator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ComputationsCollection {

    private const val COMPUTATION_CODE = "CP"

    private val computations: MutableMap<String,
        Pair<Mutex, MutableMap<String, CursorSolutions>>> = mutableMapOf()

    /** Add a new iterator of solving computation to the map, generating its id **/
    fun addIterator(solverID: String, goal: Struct, options: SolveOptions): String{
        if(!computations.containsKey(solverID)) {
            computations[solverID] = Pair(Mutex(), mutableMapOf())
        }
        val computationsMap = computations[solverID]?.second!!
        var computationID: String
        do {computationID = idGenerator() + COMPUTATION_CODE } while (computationsMap.containsKey(computationID))
        computationsMap[computationID] = CursorSolutions(SolversCollection.getSolver(solverID)
            .solve(goal, options).iterator())
        return computationID
    }

    /**
     * @return the requested solution, Solution.No if outOfBounds.
     */
    suspend fun getSolution(solverID: String, computationID: String, query: Struct, index: Int): Pair<Solution, Boolean> {
        try {
            if (computations.containsKey(solverID) && computations[solverID]?.second?.containsKey(computationID)!!) {
                computations[solverID]?.first?.withLock {
                    val iterator = computations[solverID]?.second?.get(computationID)!!
                    val solution = iterator.getSolution(index)
                    if (!solution.isYes || !iterator.hasNext()) {
                        return Pair(solution, false)
                    }
                    return Pair(solution, true)
                }
            }
        } catch (_: Exception) { }
        return Pair(Solution.no(query), false)
    }
}