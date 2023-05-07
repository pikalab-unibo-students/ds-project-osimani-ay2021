package it.unibo.tuprolog.primitives.client

import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.impl.SessionSolverImpl
import it.unibo.tuprolog.solve.ExecutionContext

interface SessionSolver {

    fun solve(event: SubSolveRequest): SolverMsg

    fun readLine(event: ReadLineMsg): SolverMsg

    companion object {
        fun of(executionContext: ExecutionContext
        ): SessionSolverImpl = SessionSolverImpl(executionContext.createSolver())
    }
}