package it.unibo.tuprolog.primitives.client.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildLineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveSolutionMsg
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver

class SessionSolverImpl(
    private val responseObserver: () -> StreamObserver<SolverMsg>,
    private val solver: Solver
): SessionSolver {

    private val computations: MutableMap<String, Iterator<Solution>> = mutableMapOf()
    override fun solve(event: SubSolveRequest) {
        val query = event.query.deserialize()
        computations.putIfAbsent(event.requestID, solver.solve(query).iterator())
        val solution: Solution = computations[event.requestID]!!.next()
        responseObserver().onNext(
            buildSubSolveSolutionMsg(solution, event.requestID,
                computations[event.requestID]!!.hasNext())
        )
    }

    override fun readLine(event: ReadLineMsg) {
            solver.inputChannels[event.channelName]?.let { channel ->
                val line = channel.read()
                if(!line.isNullOrBlank()) {
                    responseObserver().onNext(
                        buildLineMsg(event.channelName, line)
                    )
                }
            }

    }
}