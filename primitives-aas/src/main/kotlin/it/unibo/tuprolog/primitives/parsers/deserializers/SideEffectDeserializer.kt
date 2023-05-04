package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.primitives.sideEffects.*
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.sideffects.SideEffect

fun SideEffectMsg.deserialize(): SideEffect {
    when(this.msgCase) {
        SideEffectMsg.MsgCase.CLAUSES -> {
            val effect = this.clauses
            val clauses = effect.clausesList
                .map { it.deserialize().castToClause() }
            when(effect.kbType) {
                SetClausesOfKBMsg.KbType.STATIC -> {
                    when(effect.operationType) {
                        SetClausesOfKBMsg.OpType.RESET ->
                            SideEffect.ResetStaticKb(clauses)
                        SetClausesOfKBMsg.OpType.ADD ->
                            SideEffect.AddStaticClauses(clauses, effect.onTop)
                        SetClausesOfKBMsg.OpType.REMOVE ->
                            SideEffect.RemoveStaticClauses(clauses)
                        else -> {}
                    }
                }
                SetClausesOfKBMsg.KbType.DYNAMIC -> {
                    when(effect.operationType) {
                        SetClausesOfKBMsg.OpType.RESET ->
                            SideEffect.ResetDynamicKb(clauses)
                        SetClausesOfKBMsg.OpType.ADD ->
                            SideEffect.AddDynamicClauses(clauses, effect.onTop)
                        SetClausesOfKBMsg.OpType.REMOVE ->
                            SideEffect.RemoveDynamicClauses(clauses)
                        else -> {}
                    }
                }
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.FLAGS -> {
            val effect = this.flags
            val flags = effect.flagsMap.map {
                Pair(it.key, it.value.deserialize())
            }.toMap()
            when(effect.operationType) {
                AlterFlagsMsg.OpType.RESET ->
                    SideEffect.ResetFlags(flags)
                AlterFlagsMsg.OpType.SET ->
                    SideEffect.SetFlags(flags)
                AlterFlagsMsg.OpType.CLEAR ->
                    SideEffect.ClearFlags(flags.keys)
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.RUNTIME -> {
            val effect = this.runtime
            when(effect.operationType) {
                AlterRuntimeMsg.OpType.LOAD ->
                    SideEffect.LoadLibrary(Library.of(effect.getLibraries(0)))
                AlterRuntimeMsg.OpType.UNLOAD ->
                    SideEffect.UnloadLibraries(effect.librariesList)
                else -> {}
            }

        }
        SideEffectMsg.MsgCase.OPERATORS -> {
            val effect = this.operators
            val operators = effect.operatorsList.map { it.deserialize() }
            when(effect.operationType) {
                AlterOperatorsMsg.OpType.SET ->
                    SideEffect.SetOperators(operators)
                AlterOperatorsMsg.OpType.RESET ->
                    SideEffect.ResetOperators(operators)
                AlterOperatorsMsg.OpType.REMOVE ->
                    SideEffect.RemoveOperators(operators)
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.CHANNELS -> {
            val effect = this.channels
            when(effect.channelType) {
                AlterChannelsMsg.ChannelType.INPUT -> {
                    val inputs = effect.channelsMap.map {
                        Pair(it.key, InputChannel.of(it.value)
                    ) }.toMap()
                    when(effect.type) {
                        AlterChannelsMsg.OpType.OPEN ->
                            SideEffect.OpenInputChannels(inputs)
                        AlterChannelsMsg.OpType.RESET ->
                            SideEffect.ResetInputChannels(inputs)
                        AlterChannelsMsg.OpType.CLOSE ->
                            SideEffect.CloseInputChannels(inputs.keys)
                        else -> {}
                    }
                }
                AlterChannelsMsg.ChannelType.OUTPUT -> {
                    val outputs = effect.channelsMap.map {
                        Pair(it.key, OutputChannel.stdOut<String>()) }.toMap()
                    when(effect.type) {
                        AlterChannelsMsg.OpType.OPEN ->
                            SideEffect.OpenOutputChannels(outputs)
                        AlterChannelsMsg.OpType.RESET ->
                            SideEffect.ResetOutputChannels(outputs)
                        AlterChannelsMsg.OpType.CLOSE ->
                            SideEffect.CloseOutputChannels(outputs.keys)
                        else -> {}
                    }
                }
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.CUSTOMDATA -> {
            val effect = this.customData
            when(effect.type) {
                AlterCustomDataMsg.OpType.SET_PERSISTENT ->
                    SideEffect.SetPersistentData(effect.dataMap)
                AlterCustomDataMsg.OpType.SET_DURABLE ->
                    SideEffect.SetDurableData(effect.dataMap)
                AlterCustomDataMsg.OpType.SET_EPHEMERAL ->
                    SideEffect.SetEphemeralData(effect.dataMap)
                else -> {}
            }
        }
        else -> {}
    }
    throw IllegalStateException()
}