package it.unibo.tuprolog.primitives.server.session.event

import it.unibo.tuprolog.primitives.idGenerator

interface ServerEvent<A, B, C> {

    fun sendRequest(input: A): C

    fun handleResponse(response: B)
}