package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.theory.Theory

interface Session {

    /** Request the client solver to resolve a query. It can be blocking.
     * @return the solutions computed
     */
    fun subSolve(query: Struct, timeout: Long = SolveOptions.MAX_TIMEOUT): Sequence<Solution>

    /** Reads a character from an Input channel of the Solver.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun readLine(channelName: String): String

    /** Inspect the KB of the Client Solver and returns part of it, optionally applying a filter.
     * If maxSolutions is -1 no max is applied.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun inspectKB(
        kbType: KbType,
        maxClauses: Long = -1,
        vararg filters: Pair<KbFilter, String>
    ): Theory

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
