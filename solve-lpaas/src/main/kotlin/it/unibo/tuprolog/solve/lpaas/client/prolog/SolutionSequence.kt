package it.unibo.tuprolog.solve.lpaas.client.prolog

import com.google.common.util.concurrent.Futures
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.exception.TuPrologException
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.lpaas.NextSolutionRequest
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined
import java.util.concurrent.Executor
import java.util.concurrent.Future

class SolutionSequence(val solverId: String, val query: String): Sequence<Solution> {

    override fun iterator(): Iterator<Solution> {
        return object: Iterator<Solution> {

            private var nextFlag = true;

            private val clientGetSolution: SolverGrpc.SolverFutureStub =
                SolverGrpc.newFutureStub(
                    ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build())

            override fun hasNext(): Boolean {
                return nextFlag
            }

            override fun next(): Solution {
                //return GlobalScope.async(Dispatchers.IO) {getNextResult()}
                return getNextResult()
            }

            fun getNextResult(): Solution {
                val reply = clientGetSolution.nextSolution(
                    NextSolutionRequest.newBuilder()
                        .setQuery(query).setId(solverId).build()
                ).get()
                val struct = Struct.parse(query)
                val scope = Scope.of(struct.args.filter { it.isVar }.map { it.castToVar() })
                println(struct)
                return if (reply.isYes) {
                    val unifiers: MutableMap<Var, Term> = mutableMapOf()
                    reply.substitutionList.forEach { pair ->
                        unifiers[scope.varOf(pair.`var`)] = Term.parse(pair.term)
                    }
                    println(unifiers.keys + unifiers.values)
                    Solution.yes(struct, Substitution.unifier(unifiers))
                } else {
                    nextFlag = false
                    return if (reply.isNo) Solution.no(struct) else return Solution.no(struct)
                    /** Fix Error **/
                    //else Solution.halt(struct, ResolutionException(TuPrologException("")))
                }
            }
        }
    }
}