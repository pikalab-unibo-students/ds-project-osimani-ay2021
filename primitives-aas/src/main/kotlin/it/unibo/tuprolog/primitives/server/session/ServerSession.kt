package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution

interface ServerSession {

    /** Request the client solver to resolve a query. It can be blocking.
     * @return the solutions computed
     */
    fun subSolve(query: Struct): Sequence<Solution>

    /** Reads a character from an Input channel of the Solver.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun readLine(channelName: String): String

}
