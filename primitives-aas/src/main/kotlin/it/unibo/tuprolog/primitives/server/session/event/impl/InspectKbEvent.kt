package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.TheoryMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildInspectKbMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.theory.Theory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class InspectKbEvent(
    override val id: String,
    kbType: Session.KbType,
    maxClauses: Long,
    vararg filters: Pair<Session.KbFilter, String>
): SubRequestEvent {

    override val message: GeneratorMsg = buildInspectKbMsg(id, kbType, maxClauses, *filters)

    private val result: CompletableDeferred<TheoryMsg> = CompletableDeferred()

    override fun awaitResult(): Theory {
        val clauses = runBlocking {
            result.await()
        }.clausesList
        return Theory.Companion.of(clauses.map { it.deserialize().castToClause() })
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if(msg.hasTheory())
            this.result.complete(msg.theory)
        else
            throw IllegalArgumentException()
    }
}