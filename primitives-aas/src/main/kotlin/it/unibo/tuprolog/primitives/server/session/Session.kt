package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

interface Session {

    fun subSolve(query: Struct): Sequence<Solution>

    fun readLine(channelName: String): String

}
