package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator

interface ContextRequester {
    fun getLogicStackTrace(): List<Struct>

    fun getCustomDataStore(): CustomDataStore

    fun getUnificator(): Unificator

    fun getLibraries(): DistributedRuntime

    fun getFlagStore(): FlagStore

    fun getOperators(): OperatorSet

    /** Inspect the KB of the Client Solver and returns part of it, optionally applying a filter.
     * If maxSolutions is -1 no max is applied.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun inspectKB(
        kbType: Session.KbType,
        maxClauses: Long = -1,
        vararg filters: Pair<Session.KbFilter, String>
    ): Sequence<Clause?>

    fun getInputStoreAliases(): Set<String>

    fun getOutputStoreAliases(): Set<String>
}
