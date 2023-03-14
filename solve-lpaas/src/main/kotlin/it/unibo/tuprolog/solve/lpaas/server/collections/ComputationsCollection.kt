package it.unibo.tuprolog.solve.lpaas.server.collections

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.util.idGenerator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ComputationsCollection {

    private const val COMPUTATION_CODE = "CP"

    private val computations: MutableMap<String,
        Pair<Mutex, MutableMap<String, CursorSolutions>>> = mutableMapOf()
    private val parser = TermParser.withDefaultOperators()

    /** Add a new iterator of solving computation to the map, generating its id **/
    fun addIterator(solverID: String, query: String, options: SolveOptions): String{
        if(!computations.containsKey(solverID)) {
            computations[solverID] = Pair(Mutex(), mutableMapOf())
        }
        val computationsMap = computations[solverID]?.second!!
        var computationID: String
        do {computationID = idGenerator() + COMPUTATION_CODE } while (computationsMap.containsKey(computationID))
        computationsMap[computationID] = CursorSolutions(SolversCollection.getSolver(solverID)
            .solve(parser.parseStruct(query), options).iterator())
        return computationID
    }

    /**
     * @return the requested solution, Solution.No if outOfBounds.
     */
    suspend fun getSolution(solverID: String, computationID: String, query: String, index: Int): Solution {
        try {
            if (computations.containsKey(solverID) && computations[solverID]?.second?.containsKey(computationID)!!) {
                computations[solverID]?.first?.withLock {
                    val iterator = computations[solverID]?.second?.get(computationID)!!
                    val solution = iterator.getSolution(index)
                    if (!solution.isYes || !iterator.hasNext()) {
                        computations[solverID]?.second?.remove(computationID)
                    }
                    return solution
                }
            }
        } catch (_: Exception) { }
        return Solution.no(Struct.parse(query))
    }
}