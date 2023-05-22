package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.sideffects.SideEffect

data class DistributedRequest(
    val signature: Signature,
    val arguments: List<Term>,
    val context: DistributedExecutionContext,
    private val session: ServerSession
): Session {

    val query: Struct by lazy { signature withArgs arguments }

    fun replyWith(
        condition: Boolean,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        if (condition) DistributedSolution.yes(query) else DistributedSolution.no(query),
        sideEffect.asList())


    fun replySuccess(
        substitution: Substitution.Unifier,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        DistributedSolution.yes(query, substitution),
        sideEffect.asList()
    )

    fun replyFail(
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        DistributedSolution.no(query),
        sideEffect.asList()
    )

    fun replyError(
        e: DistributedError,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
            DistributedSolution.halt(query, e),
        sideEffect.asList()
    )

    override fun subSolve(
        query: Struct,
        timeout: Long
    ): Sequence<DistributedResponse> =
        session.subSolve(query, timeout)

    override fun readLine(channelName: String): String =
        session.readLine(channelName)
}