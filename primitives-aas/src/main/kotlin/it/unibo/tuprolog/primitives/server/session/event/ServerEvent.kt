package it.unibo.tuprolog.primitives.server.session.event

interface ServerEvent<A, B, C> {

    fun sendRequest(input: A): C

    fun handleResponse(response: B)
}