package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SubSolveResponse
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineHandler
import it.unibo.tuprolog.primitives.server.session.event.impl.SubSolveHandler
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction1

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class SessionObserver(
    emit: KSuspendFunction1<GeneratorMsg, Unit>
): Session {

    private val subSolveHandler: SubSolveHandler = SubSolveHandler(emit)
    private val readLineHandler: ReadLineHandler = ReadLineHandler(emit)

    override fun subSolve(query: Struct): Sequence<Solution> =
        runBlocking {
            subSolveHandler.applyEvent(query)
        }

    fun addSolution(msg: SubSolveResponse) = subSolveHandler.handleResponse(msg)

    override fun readLine(channelName: String): String =
        runBlocking {
            readLineHandler.applyEvent(channelName)
        }

    fun addLine(msg: LineMsg) = readLineHandler.handleResponse(msg)

}