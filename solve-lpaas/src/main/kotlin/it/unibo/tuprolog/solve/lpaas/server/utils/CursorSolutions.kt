package it.unibo.tuprolog.solve.lpaas.server.utils

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.lpaas.SolutionID
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class CursorSolutions(private val iterator: Iterator<Solution>) :
    Iterator<Solution> {

    private val solutionsCache: MutableList<Solution> = mutableListOf()

    /** Generate all solutions till the requested one **/
    fun getSolution(index: Int): Solution {
        while(solutionsCache.size <= index) {
            next()
        }
        return solutionsCache[index]
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): Solution {
        if(hasNext()) {
            val solution = iterator.next()
            solutionsCache.add(solution)
            return solution
        } else throw IndexOutOfBoundsException()
    }
}