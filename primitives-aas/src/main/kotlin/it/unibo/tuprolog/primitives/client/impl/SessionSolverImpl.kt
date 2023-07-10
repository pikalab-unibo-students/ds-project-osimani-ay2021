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
import it.unibo.tuprolog.primitives.parsers.serializers.*
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.solve.sideffects.SideEffectsBuilder

class SessionSolverImpl(
    private val responseObserver: StreamObserver<SolverMsg>,
    private val actualContext: ExecutionContext
): SessionSolver {

    private val sessionSolver: Solver = actualContext.createSolver()

    private val computations: MutableMap<String, Iterator<Solution>> = mutableMapOf()

    private val theoryIterator: MutableMap<String, Iterator<Clause>> = mutableMapOf()

    override fun solve(id: String, event: SubSolveRequest) {
        val query = event.query.deserialize()
        computations.putIfAbsent(id, sessionSolver.solve(query, event.timeout).iterator())
        val solution: Solution = computations[id]!!.next()
        responseObserver.onNext(
            buildSubSolveSolutionMsg(
                id,
                Solve.Response(solution),
                computations[id]!!.hasNext()
            )
        )
    }

    override fun readLine(id: String, event: ReadLineMsg) {
        sessionSolver.inputChannels[event.channelName]?.let { channel ->
            val line = channel.read()
            responseObserver.onNext(buildLineMsg(id, event.channelName, line.orEmpty()))
        }
    }

    override fun inspectKb(id: String, event: InspectKbMsg) {
        if(!theoryIterator.containsKey(id)) {
            val inspectedKB = when(event.kbType) {
                InspectKbMsg.KbType.STATIC -> sessionSolver.staticKb
                InspectKbMsg.KbType.DYNAMIC -> sessionSolver.dynamicKb
                InspectKbMsg.KbType.BOTH -> sessionSolver.staticKb + sessionSolver.dynamicKb
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
                                clause.head!!.toString().startsWith(filter.argument, true)
                            else false
                        }
                    }
                    else -> throw IllegalArgumentException()
                }
            }
            val iterator = inspectedKB.filter {
                filters.all {filter -> filter(it) }
            }
            theoryIterator[id] =
                if(event.maxClauses.toInt() == -1)
                    iterator.iterator()
                else
                    iterator.take(event.maxClauses.toInt()).iterator()
        }

        responseObserver.onNext(
            buildClauseMsg(
                id,
                if(theoryIterator[id]!!.hasNext()) {
                    theoryIterator[id]!!.next()
                } else {
                    theoryIterator.remove(id)
                    null
                }
            )
        )
    }

    override fun getLogicStackTrace(id: String) {
        responseObserver.onNext(buildLogicStackTraceResponse(id, actualContext.logicStackTrace))
    }

    override fun getCustomDataStore(id: String) {
        responseObserver.onNext(buildCustomDataStoreResponse(id, actualContext.customData))
    }

    override fun getUnificator(id: String) {
        responseObserver.onNext(buildUnificatorResponse(id, sessionSolver.unificator))
    }

    override fun getLibraries(id: String) {
        responseObserver.onNext(buildLibrariesResponse(id, sessionSolver.libraries))
    }

    override fun getFlagStore(id: String) {
        responseObserver.onNext(buildFlagStoreResponse(id, sessionSolver.flags))
    }

    override fun getOperators(id: String) {
        responseObserver.onNext(buildOperatorsResponse(id, sessionSolver.operators))
    }

    override fun getInputStoreAliases(id: String) {
        responseObserver.onNext(
            buildChannelResponse(id, sessionSolver.inputChannels.map { it.key })
        )
    }

    override fun getOutputStoreAliases(id: String) {
        responseObserver.onNext(
            buildChannelResponse(id, sessionSolver.outputChannels.map { it.key })
        )
    }
}