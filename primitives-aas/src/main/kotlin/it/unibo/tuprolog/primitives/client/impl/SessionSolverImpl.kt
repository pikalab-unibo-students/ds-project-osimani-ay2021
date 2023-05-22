package it.unibo.tuprolog.primitives.client.impl

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.InspectKbMsg
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildLineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveSolutionMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildTheoryMsg
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.primitive.Solve

class SessionSolverImpl(
    private val responseObserver: StreamObserver<SolverMsg>,
    private val solver: Solver
): SessionSolver {

    private val computations: MutableMap<String, Iterator<Solution>> = mutableMapOf()

    override fun solve(id: String, event: SubSolveRequest) {
        val query = event.query.deserialize()
        computations.putIfAbsent(id, solver.solve(query, event.timeout).iterator())
        val solution: Solution = computations[id]!!.next()
        responseObserver.onNext(
            buildSubSolveSolutionMsg(
                id, Solve.Response(solution),
                computations[id]!!.hasNext()
            )
        )
    }

    override fun readLine(id: String, event: ReadLineMsg) {
        solver.inputChannels[event.channelName]?.let { channel ->
            val line = channel.read()
            responseObserver.onNext(buildLineMsg(id, event.channelName, line.orEmpty()))
        }
    }

    override fun inspectKb(id: String, event: InspectKbMsg) {
        val inspectedKB = when(event.kbType) {
            InspectKbMsg.KbType.STATIC -> solver.staticKb
            InspectKbMsg.KbType.DYNAMIC -> solver.dynamicKb
            else -> throw IllegalArgumentException()
        }
        val filters = event.filtersList.map { filter ->
            when(filter.type) {
                InspectKbMsg.FilterType.CONTAINS_FUNCTOR -> {
                    {clause: Clause ->
                        clause.bodyItems.any {
                            it.isStruct && it.castToStruct().functor == filter.argument
                        }}
                }
                InspectKbMsg.FilterType.CONTAINS_TERM -> {
                    {clause: Clause ->
                        clause.bodyItems.any {
                            it.structurallyEquals(Term.parse(filter.argument))
                        }}
                }
                InspectKbMsg.FilterType.STARTS_WITH -> {
                    { clause: Clause ->
                        if(clause.head != null)
                            clause.head!!.functor.startsWith(filter.argument, true)
                        else false
                    }
                }
                else -> throw IllegalArgumentException()
            }
        }
        responseObserver.onNext(
            buildTheoryMsg(
                id,
                inspectedKB.filter {
                    filters.all {filter -> filter(it) }
                }
            )
        )
    }
}