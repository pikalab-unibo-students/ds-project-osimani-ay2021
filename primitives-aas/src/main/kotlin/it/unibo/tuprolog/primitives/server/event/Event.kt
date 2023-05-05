package it.unibo.tuprolog.primitives.server.event

interface ServerEvent<A, B, C> {

    fun sendRequest(input: A): C

    fun handleResponse(response: B)
}