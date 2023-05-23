package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedResponse
import it.unibo.tuprolog.solve.SolveOptions

interface Session {

    /** Request the client solver to resolve a query. It can be blocking.
     * @return the solutions computed
     */
    fun subSolve(query: Struct, timeout: Long = SolveOptions.MAX_TIMEOUT): Sequence<DistributedResponse>

    /** Reads a character from an Input channel of the Solver.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun readLine(channelName: String): String


    enum class KbType {
        STATIC,
        DYNAMIC
    }

    enum class KbFilter {
        CONTAINS_TERM,
        STARTS_WITH,
        CONTAINS_FUNCTOR
    }
}
