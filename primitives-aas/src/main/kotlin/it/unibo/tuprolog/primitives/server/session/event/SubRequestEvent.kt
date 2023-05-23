package it.unibo.tuprolog.primitives.server.session.event

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SubResponseMsg

interface SubRequestEvent {

    val message: GeneratorMsg

    val id: String

    fun signalResponse(msg: SubResponseMsg)

    fun awaitResult(): Any?
}