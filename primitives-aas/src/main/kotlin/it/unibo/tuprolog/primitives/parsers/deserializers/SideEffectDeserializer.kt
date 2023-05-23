package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.sideEffects.*
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
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
                            return SideEffect.ResetStaticKb(clauses)
                        SetClausesOfKBMsg.OpType.ADD ->
                            return SideEffect.AddStaticClauses(clauses, effect.onTop)
                        SetClausesOfKBMsg.OpType.REMOVE ->
                            return SideEffect.RemoveStaticClauses(clauses)
                        else -> {}
                    }
                }
                SetClausesOfKBMsg.KbType.DYNAMIC -> {
                    when(effect.operationType) {
                        SetClausesOfKBMsg.OpType.RESET ->
                            return SideEffect.ResetDynamicKb(clauses)
                        SetClausesOfKBMsg.OpType.ADD ->
                            return SideEffect.AddDynamicClauses(clauses, effect.onTop)
                        SetClausesOfKBMsg.OpType.REMOVE ->
                            return SideEffect.RemoveDynamicClauses(clauses)
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
                    return SideEffect.ResetFlags(flags)
                AlterFlagsMsg.OpType.SET ->
                    return SideEffect.SetFlags(flags)
                AlterFlagsMsg.OpType.CLEAR ->
                    return SideEffect.ClearFlags(flags.keys)
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.RUNTIME -> {
            val effect = this.runtime
            when(effect.operationType) {
                AlterRuntimeMsg.OpType.LOAD ->
                    return SideEffect.LoadLibrary(Library.of(effect.getLibraries(0)))
                AlterRuntimeMsg.OpType.UNLOAD ->
                    return SideEffect.UnloadLibraries(effect.librariesList)
                //To Solve
                AlterRuntimeMsg.OpType.RESET ->
                    return SideEffect.ResetRuntime(Runtime.empty())
                else -> {}
            }

        }
        SideEffectMsg.MsgCase.OPERATORS -> {
            val effect = this.operators
            val operators = effect.operatorsList.map { it.deserialize() }
            when(effect.operationType) {
                AlterOperatorsMsg.OpType.SET ->
                    return SideEffect.SetOperators(operators)
                AlterOperatorsMsg.OpType.RESET ->
                    return SideEffect.ResetOperators(operators)
                AlterOperatorsMsg.OpType.REMOVE ->
                    return SideEffect.RemoveOperators(operators)
                else -> {}
            }
        }
        SideEffectMsg.MsgCase.CHANNELS -> {
            val effect = this.channels
            if(effect.hasClose()) {
                when(effect.close.channelType) {
                    AlterChannelsMsg.ChannelType.INPUT -> {
                        return SideEffect.CloseInputChannels(effect.close.channelsList)
                    }
                    AlterChannelsMsg.ChannelType.OUTPUT -> {
                        return SideEffect.CloseOutputChannels(effect.close.channelsList)
                    }
                    else -> {}
                }
            } else if (effect.hasModify()) {
                when(effect.modify.channelType) {
                    AlterChannelsMsg.ChannelType.INPUT -> {
                        val inputs = effect.modify.channelsMap.map {
                            Pair(it.key, InputChannel.of(it.value)
                            ) }.toMap()
                        when(effect.modify.opType) {
                            AlterChannelsMsg.ModifyChannels.OpType.OPEN -> {
                                return SideEffect.OpenInputChannels(inputs)
                            }
                            AlterChannelsMsg.ModifyChannels.OpType.RESET -> {
                                return SideEffect.ResetInputChannels(inputs)
                            }
                            else -> {}
                        }
                    }
                    AlterChannelsMsg.ChannelType.OUTPUT -> {
                        val outputs = effect.modify.channelsMap.map {
                            Pair(it.key, OutputChannel.of<String> {  }) }
                            .toMap()
                        when(effect.modify.opType) {
                            AlterChannelsMsg.ModifyChannels.OpType.OPEN -> {
                                return SideEffect.OpenOutputChannels(outputs)
                            }
                            AlterChannelsMsg.ModifyChannels.OpType.RESET -> {
                                return SideEffect.ResetOutputChannels(outputs)
                            }
                            else -> {}
                        }
                    }
                    else -> {}
                }
            } else if (effect.hasWrite()) {
                return SideEffect.WriteOnOutputChannels(
                    effect.write.messagesMap.mapValues {
                        it.value.messageList
                    })
            }
        }
        SideEffectMsg.MsgCase.CUSTOMDATA -> {
            val effect = this.customData
            when(effect.type) {
                AlterCustomDataMsg.OpType.SET_PERSISTENT ->
                    return SideEffect.SetPersistentData(effect.dataMap)
                AlterCustomDataMsg.OpType.SET_DURABLE ->
                    return SideEffect.SetDurableData(effect.dataMap)
                AlterCustomDataMsg.OpType.SET_EPHEMERAL ->
                    return SideEffect.SetEphemeralData(effect.dataMap)
                else -> {}
            }
        }
        else -> {}
    }
    throw ParsingException(this)
}