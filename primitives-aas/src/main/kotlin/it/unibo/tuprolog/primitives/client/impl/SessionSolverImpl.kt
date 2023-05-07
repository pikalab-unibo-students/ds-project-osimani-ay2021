package it.unibo.tuprolog.primitives.client.impl

import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildLineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveSolutionMsg
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver

class SessionSolverImpl(
    private val solver: Solver
): SessionSolver {

    private val computations: MutableMap<String, Iterator<Solution>> = mutableMapOf()
    override fun solve(event: SubSolveRequest): SolverMsg {
        val query = event.query.deserialize()
        computations.putIfAbsent(event.requestID, solver.solve(query).iterator())
        val solution: Solution = computations[event.requestID]!!.next()
        return buildSubSolveSolutionMsg(solution, event.requestID,
                computations[event.requestID]!!.hasNext())
    }

    override fun readLine(event: ReadLineMsg): SolverMsg {
        val channel = solver.inputChannels[event.channelName]
        return try {
            val line = channel!!.read()
            if(line.isNullOrBlank()) buildLineMsg(event.channelName, error = LineMsg.Error.EMPTY_CHANNEL)
            else buildLineMsg(event.channelName, line = line)
        } catch (_: Exception) {
            buildLineMsg(event.channelName, error = LineMsg.Error.CHANNEL_NOT_FOUND)
        }

    }
}