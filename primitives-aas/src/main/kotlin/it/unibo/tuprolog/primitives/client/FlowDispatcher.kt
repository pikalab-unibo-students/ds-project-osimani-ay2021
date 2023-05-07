package it.unibo.tuprolog.primitives.client

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.solve.primitive.Solve

interface FlowDispatcher {

    suspend fun handleMessage(msg: GeneratorMsg)

    suspend fun getMessage(): SolverMsg

    suspend fun popResponse(): Solve.Response

    val isClosed: Boolean
}