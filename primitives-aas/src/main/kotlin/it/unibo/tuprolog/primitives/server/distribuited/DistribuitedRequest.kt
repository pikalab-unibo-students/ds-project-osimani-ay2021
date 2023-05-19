package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.sideffects.SideEffect
import it.unibo.tuprolog.theory.Theory

data class DistribuitedRequest(
    val signature: Signature,
    val arguments: List<Term>,
    val context: ExecutionContext,
    val session: ServerSession
): Session {

    val query: Struct by lazy { signature withArgs arguments }

    fun replyWith(
        condition: Boolean,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        if (condition) Solution.yes(query) else Solution.no(query),
        sideEffect.asList())


    fun replySuccess(
        substitution: Substitution.Unifier,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        Solution.yes(query, substitution),
        sideEffect.asList()
    )

    fun replyFail(
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        Solution.no(query),
        sideEffect.asList()
    )

    fun replyError(
        e: Throwable,
        vararg sideEffect: SideEffect
    ) = DistributedResponse(
        Solution.halt(query, ResolutionException(e, context)),
        sideEffect.asList()
    )

    override fun subSolve(
        query: Struct,
        timeout: Long
    ): Sequence<Solution> =
        session.subSolve(query, timeout)

    override fun readLine(channelName: String): String =
        session.readLine(channelName)

    override fun inspectKB(
        kbType: Session.KbType,
        maxClauses: Long,
        vararg filters: Pair<Session.KbFilter, String>
    ): Theory =
        session.inspectKB(kbType, maxClauses, *filters)
}