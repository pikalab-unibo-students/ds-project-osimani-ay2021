package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.server.session.ContextRequester
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.primitives.utils.DummyContext
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.TimeInstant
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

data class DistributedExecutionContext(
    val procedure: Struct,
    val substitution: Substitution.Unifier,
    val startTime: Long,
    val endTime: Long,
    val remainingTime: Long,
    val elapsedTime: Long,
    val maxDuration: Long,
    private val contextRequester: ContextRequester
) {

    val logicStackTrace: List<Struct>
        get() = contextRequester.getLogicStackTrace()
    val customData: CustomDataStore
        get() = contextRequester.getCustomDataStore()
    val unificator: Unificator
        get() = contextRequester.getUnificator()
    val runtime: DistributedRuntime
        get() = contextRequester.getLibraries()
    val flags: FlagStore
        get() = contextRequester.getFlagStore()
    val staticKb: Theory
        get() = Theory.of(contextRequester.inspectKB(Session.KbType.STATIC).filterNotNull().toList())
    val dynamicKb: Theory
        get() = Theory.of(contextRequester.inspectKB(Session.KbType.DYNAMIC).filterNotNull().toList())
    val operators: OperatorSet
        get() = contextRequester.getOperators()
    val inputStore: Set<String>
        get() = contextRequester.getInputStoreAliases()
    val outputStore: Set<String>
        get() = contextRequester.getOutputStoreAliases()

    fun toDummyContext(): ExecutionContext {
        val source = this
        return object: DummyContext() {
            override val procedure: Struct = source.procedure
            override val startTime: TimeInstant = source.startTime
            override val maxDuration: TimeDuration = source.maxDuration
            override val substitution: Substitution.Unifier = source.substitution
            override val operators: OperatorSet = OperatorSet.EMPTY
        }
    }
}