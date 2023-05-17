package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SolutionMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class SingleSubSolveEvent(
    override val id: String,
    private val query: Struct,
    timeout: Long
): SubRequestEvent {

    override val message: GeneratorMsg = buildSubSolveMsg(query, id, timeout = timeout)

    private val result: CompletableDeferred<SolutionMsg> = CompletableDeferred()
    private var hasNext: Boolean? = null

    fun hasNext(): Boolean? = hasNext

    override fun awaitResult(): Solution {
        val solution = runBlocking {
            result.await()
        }
        hasNext = solution.hasNext
        return solution.deserialize(Scope.of(query))
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if(msg.hasSolution())
            this.result.complete(msg.solution)
        else
            throw IllegalArgumentException()
    }
}