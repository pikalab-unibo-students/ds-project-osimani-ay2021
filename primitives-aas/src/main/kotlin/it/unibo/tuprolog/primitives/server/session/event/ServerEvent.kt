package it.unibo.tuprolog.primitives.server.session.event

interface ServerEvent<A, B, C> {

    suspend fun applyEvent(input: A): C

    fun handleResponse(response: B)
}