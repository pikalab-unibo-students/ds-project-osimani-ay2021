package it.unibo.tuprolog.primitives.server.session.event

import it.unibo.tuprolog.primitives.GeneratorMsg
import kotlin.reflect.KSuspendFunction1

interface ServerEvent<A, B, C> {

    suspend fun applyEvent(input: A): C

    suspend fun handleResponse(response: B)

}