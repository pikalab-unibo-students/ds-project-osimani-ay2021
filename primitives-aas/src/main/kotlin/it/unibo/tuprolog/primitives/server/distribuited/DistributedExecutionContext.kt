package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.server.session.ContextRequester
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
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
    val libraries: List<String>
        get() = contextRequester.getLibraries()
    val flags: FlagStore
        get() = contextRequester.getFlagStore()
    val staticKb: Theory
        get() = contextRequester.inspectKB(Session.KbType.STATIC)
    val dynamicKb: Theory
        get() = contextRequester.inspectKB(Session.KbType.DYNAMIC)
    val operators: OperatorSet
        get() = contextRequester.getOperators()
    val channels: Map<ContextRequester.ChannelType, String>
        get() = contextRequester.getChannelsNames()

    fun toDummyContext(): ExecutionContext {
        val source = this
        return object: ExecutionContext {
            override val procedure: Struct = source.procedure
            override val startTime: TimeInstant = source.startTime
            override val maxDuration: TimeDuration = source.maxDuration
            override val substitution: Substitution.Unifier = source.substitution
            override val logicStackTrace: List<Struct> = emptyList()
            override val customData: CustomDataStore = CustomDataStore.empty()
            override val unificator: Unificator = Unificator.default
            override val libraries: Runtime = Runtime.empty()
            override val flags: FlagStore = FlagStore.empty()
            override val staticKb: Theory = Theory.empty(unificator)
            override val dynamicKb: Theory = Theory.empty(unificator)
            override val operators: OperatorSet = OperatorSet.EMPTY
            override val inputChannels: InputStore = InputStore.fromStandard()
            override val outputChannels: OutputStore = OutputStore.fromStandard()

            override fun createSolver(
                unificator: Unificator,
                libraries: Runtime,
                flags: FlagStore,
                staticKb: Theory,
                dynamicKb: Theory,
                inputChannels: InputStore,
                outputChannels: OutputStore
            ): Solver {
                TODO("Not yet implemented")
            }

            override fun createMutableSolver(
                unificator: Unificator,
                libraries: Runtime,
                flags: FlagStore,
                staticKb: Theory,
                dynamicKb: Theory,
                inputChannels: InputStore,
                outputChannels: OutputStore
            ): MutableSolver {
                TODO("Not yet implemented")
            }

            override fun update(
                unificator: Unificator,
                libraries: Runtime,
                flags: FlagStore,
                staticKb: Theory,
                dynamicKb: Theory,
                operators: OperatorSet,
                inputChannels: InputStore,
                outputChannels: OutputStore,
                customData: CustomDataStore
            ): ExecutionContext {
                TODO("Not yet implemented")
            }
        }
    }
}