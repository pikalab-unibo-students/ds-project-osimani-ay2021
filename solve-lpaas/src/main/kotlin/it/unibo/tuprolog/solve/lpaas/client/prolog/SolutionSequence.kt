package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.lpaas.NextSolutionRequest
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolverGrpc

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
                val reply: SolutionReply = clientGetSolution.nextSolution(
                    NextSolutionRequest.newBuilder()
                    .setQuery(query).setId(solverId).build()
                ).get()
                val struct = Struct.parse(query)
                return if(reply.isYes) {
                    /** Fix Substitutions**/
                    Solution.yes(struct, Substitution.empty())
                } else {
                    nextFlag = false
                    return if (reply.isNo) Solution.no(struct)
                    /** Fix Error**/
                    else  Solution.no(struct)
                }
            }
        }
    }
}