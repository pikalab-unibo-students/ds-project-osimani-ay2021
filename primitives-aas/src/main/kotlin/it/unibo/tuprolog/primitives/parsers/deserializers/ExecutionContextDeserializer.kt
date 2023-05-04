package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.messages.CustomDataMsg
import it.unibo.tuprolog.primitives.messages.ExecutionContextMsg
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.classic.ClassicExecutionContext
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.theory.MutableTheory
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

fun ExecutionContextMsg.deserialize(scope: Scope = Scope.empty()): ExecutionContext {
    val msg = this
    val unificator = Unificator.naive(
        Substitution.of(this.unificatorMap.map {
            Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
        }.toMap()))
    return object: ExecutionContext {
        override val procedure = msg.procedure.deserialize(scope)
        override val substitution = Substitution.of(msg.substitutionsMap.map {
            Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
        }.toMap())
        override val logicStackTrace = msg.logicStackTraceList.map {
            it.deserialize()
        }
        override val customData = msg.customDataStore.deserialize()
        override val unificator = unificator
        override val libraries = Runtime.of(msg.librariesList.map
            { Library.of(it) }) //FIX
        override val flags = FlagStore.of(
            msg.flagsMap.map
            { Pair(it.key, it.value.deserialize()) }.toMap())
        override val staticKb = Theory.of(msg.staticKBList.map
            { it.deserialize().castToClause() })
        override val dynamicKb = MutableTheory.of(unificator, msg.dynamicKBList.map
            { it.deserialize().castToClause()  })
        override val operators = OperatorSet(msg.operatorsList.map
            { it.deserialize() })
        override val inputChannels = InputStore.of(msg.inputChannelsMap.map
            {
                Pair(it.key, InputChannel.of(it.value))
            }.toMap())
        override val outputChannels = OutputStore.of(msg.outputChannelsList.associateWith
            {
                OutputChannel.of { }
            })
        override val startTime = msg.startTime
        override val endTime = msg.endTime
        override val remainingTime = msg.remainingTime
        override val elapsedTime = msg.elapsedTime
        override val maxDuration = msg.maxDuration

        override fun createSolver(
            unificator: Unificator,
            libraries: Runtime,
            flags: FlagStore,
            staticKb: Theory,
            dynamicKb: Theory,
            inputChannels: InputStore,
            outputChannels: OutputStore
        ): Solver =
            throw NotImplementedError()

        override fun createMutableSolver(
            unificator: Unificator,
            libraries: Runtime,
            flags: FlagStore,
            staticKb: Theory,
            dynamicKb: Theory,
            inputChannels: InputStore,
            outputChannels: OutputStore
        ): MutableSolver =
            throw NotImplementedError()

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
            throw NotImplementedError()
        }
    }
}



/* Fix with generic type value */
fun CustomDataMsg.deserialize(): CustomDataStore = CustomDataStore.empty().copy(
    this.persistentDataMap,
    this.durableDataMap,
    this.ephemeralDataMap
)