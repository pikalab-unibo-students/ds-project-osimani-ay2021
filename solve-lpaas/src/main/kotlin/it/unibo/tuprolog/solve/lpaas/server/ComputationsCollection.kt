package it.unibo.tuprolog.solve.lpaas.server

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions

object ComputationsCollection {

    private val computations: MutableMap<String, Pair<String, Iterator<Solution>>> = mutableMapOf()
    private val parser = TermParser.withDefaultOperators()

    fun addIterator(solverId: String, query: String, options: SolveOptions){
        val solutionIterator = SolversCollection.getSolver(solverId).solve(parser.parseStruct(query), options).iterator()
        computations[solverId] = Pair(query, solutionIterator)
    }

    fun getNextSolution(solverId: String, query: String): Solution {
        if(computations.containsKey(solverId) && computations[solverId]?.first == query) {
            val iterator = computations[solverId]?.second!!
            val solution = iterator.next()
            if ( !solution.isYes || !iterator.hasNext()) {
                computations.remove(solverId)
            }
            return solution
        }
        return Solution.no(Struct.parse(query))
    }
}