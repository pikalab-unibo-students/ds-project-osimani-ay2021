package it.unibo.tuprolog.solve.lpaas.client.prolog

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

class SolutionsSequence(private val solverID: String, private val computationID: String, private val query: String,
    channel: ManagedChannel
):
    Iterator<Solution> {

    private val solutionsCache: MutableList<Solution> = mutableListOf()
    private val newSolutionsQueue: BlockingDeque<Solution> = LinkedBlockingDeque()
    private val stub: SolverGrpc.SolverStub = SolverGrpc.newStub(channel)

    private var hasNextFlag: Boolean = true

    private val solutionStream = stub.getSolution(
        object: StreamObserver<SolutionReply> {

            private val struct = Struct.parse(query)

            override fun onNext(value: SolutionReply) {
                val scope = Scope.of(struct.args.filter { it.isVar }.map { it.castToVar() })
                if (value.isYes) {
                    val unifiers: MutableMap<Var, Term> = mutableMapOf()
                    value.substitutionList.forEach { pair ->
                        unifiers[scope.varOf(pair.`var`)] = Term.parse(pair.term)
                    }
                    newSolutionsQueue.putLast(Solution.yes(struct, Substitution.unifier(unifiers)))
                } else {
                    if (value.isNo) newSolutionsQueue.putLast(Solution.no(struct))
                    /** Fix Error **/
                    else newSolutionsQueue.putLast(Solution.no(struct))
                    hasNextFlag = false

                    if(value.error.isNotEmpty()) println(value.error)
                }
            }

            override fun onError(t: Throwable?) {
                println(t.toString())
            }

            override fun onCompleted() {}
    })

    fun getSolution(index: Int): Solution {
        while(solutionsCache.size <= index) {
            next()
        }
        return solutionsCache[index]
    }

    override fun next(): Solution {
        if(hasNext()) {
            solutionStream.onNext(
                SolutionID.newBuilder().setSolverID(solverID).setComputationID(computationID)
                    .setQuery(query).setIndex(solutionsCache.size).build()
            )
            solutionsCache.add(newSolutionsQueue.takeFirst())
            return solutionsCache.last()
        } else throw IndexOutOfBoundsException()
    }

    override fun hasNext(): Boolean {
        return hasNextFlag
    }

    fun closeSequence() {
        solutionStream.onCompleted()
    }
}