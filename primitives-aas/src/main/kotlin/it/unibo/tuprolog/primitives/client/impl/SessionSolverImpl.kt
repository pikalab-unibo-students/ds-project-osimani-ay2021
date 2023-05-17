package it.unibo.tuprolog.primitives.client.impl

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildLineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveSolutionMsg
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.Solver

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
                id, solution,
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
}