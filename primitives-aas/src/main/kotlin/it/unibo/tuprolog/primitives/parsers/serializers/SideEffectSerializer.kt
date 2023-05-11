package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.sideEffects.*
import it.unibo.tuprolog.primitives.sideEffects.AlterChannelsMsg.CloseChannels
import it.unibo.tuprolog.primitives.sideEffects.AlterChannelsMsg.ModifyChannels
import it.unibo.tuprolog.solve.sideffects.SideEffect

fun SideEffect.serialize(): SideEffectMsg =
    when (this) {
        is SideEffect.SetClausesOfKb -> this.serialize()
        is SideEffect.AlterFlags -> this.serialize()
        is SideEffect.AlterRuntime -> this.serialize()
        is SideEffect.AlterOperators -> this.serialize()
        is SideEffect.AlterChannels -> this.serialize()
        is SideEffect.AlterCustomData -> this.serialize()
        else -> throw ParsingException(this)
    }


fun SideEffect.SetClausesOfKb.serialize(): SideEffectMsg {
    val builder = SetClausesOfKBMsg.newBuilder()
        .addAllClauses(clauses.map { it.serialize() })
    when(this) {
        is SideEffect.AddStaticClauses ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.STATIC)
                .setOperationType(SetClausesOfKBMsg.OpType.ADD)
                .setOnTop(this.onTop)
        is SideEffect.ResetStaticKb ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.STATIC)
                .setOperationType(SetClausesOfKBMsg.OpType.RESET)
        is SideEffect.RemoveStaticClauses ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.STATIC)
                .setOperationType(SetClausesOfKBMsg.OpType.REMOVE)
        is SideEffect.AddDynamicClauses ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.DYNAMIC)
                .setOperationType(SetClausesOfKBMsg.OpType.ADD)
                .setOnTop(this.onTop)
        is SideEffect.ResetDynamicKb ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.DYNAMIC)
                .setOperationType(SetClausesOfKBMsg.OpType.RESET)
        is SideEffect.RemoveDynamicClauses ->
            builder
                .setKbType(SetClausesOfKBMsg.KbType.DYNAMIC)
                .setOperationType(SetClausesOfKBMsg.OpType.REMOVE)
    }
    return SideEffectMsg.newBuilder()
        .setClauses(builder).build()
}

fun SideEffect.AlterFlags.serialize(): SideEffectMsg {
    val builder = when(this) {
        is SideEffect.AlterFlagsByEntries -> {
            val builder = AlterFlagsMsg.newBuilder()
                .putAllFlags(this.flags.map { Pair(it.key, it.value.serialize()) }.toMap())
            when (this) {
                is SideEffect.SetFlags ->
                    builder
                        .setOperationType(AlterFlagsMsg.OpType.SET)
                is SideEffect.ResetFlags ->
                    builder
                        .setOperationType(AlterFlagsMsg.OpType.RESET)
                else -> throw IllegalStateException()
            }
        }
        is SideEffect.AlterFlagsByName ->
            AlterFlagsMsg.newBuilder()
                .putAllFlags(this.names.associateWith { ArgumentMsg.getDefaultInstance() })
                .setOperationType(AlterFlagsMsg.OpType.CLEAR)
        else -> throw IllegalStateException()
    }
    return SideEffectMsg.newBuilder()
        .setFlags(builder).build()
}

fun SideEffect.AlterRuntime.serialize(): SideEffectMsg {
    val builder = AlterRuntimeMsg.newBuilder()
    when(this) {
        is SideEffect.LoadLibrary ->
           builder.setOperationType(AlterRuntimeMsg.OpType.LOAD)
               .addAllLibraries(this.libraries.aliases)
        is SideEffect.AddLibraries ->
        builder.setOperationType(AlterRuntimeMsg.OpType.LOAD)
            .addAllLibraries(this.libraries.aliases)
        is SideEffect.UnloadLibraries ->
            builder.setOperationType(AlterRuntimeMsg.OpType.UNLOAD)
                .addAllLibraries(this.aliases)
        is SideEffect.UpdateLibrary ->
            builder.setOperationType(AlterRuntimeMsg.OpType.UPDATE)
                .addAllLibraries(this.libraries.aliases)
        is SideEffect.ResetRuntime ->
            builder.setOperationType(AlterRuntimeMsg.OpType.RESET)
                .addAllLibraries(this.libraries.aliases)
        else -> throw IllegalStateException()
    }
    return SideEffectMsg.newBuilder()
        .setRuntime(builder).build()
}

fun SideEffect.AlterOperators.serialize(): SideEffectMsg {
    val builder = AlterOperatorsMsg.newBuilder()
        .addAllOperators(this.operatorSet.serialize())
    when(this) {
        is SideEffect.SetOperators ->
            builder.setOperationType(AlterOperatorsMsg.OpType.SET)
        is SideEffect.ResetOperators ->
            builder.setOperationType(AlterOperatorsMsg.OpType.RESET)
        is SideEffect.RemoveOperators ->
            builder.setOperationType(AlterOperatorsMsg.OpType.REMOVE)
        else -> throw IllegalStateException()
    }
    return SideEffectMsg.newBuilder()
        .setOperators(builder).build()
}

fun SideEffect.AlterChannels.serialize(): SideEffectMsg {
    val builder = AlterChannelsMsg.newBuilder()
    when(this) {
        is SideEffect.AlterChannelsByName -> {
            builder.setClose(CloseChannels.newBuilder()
                .addAllChannels(this.names)
                .setChannelType(
                    when(this) {
                        is SideEffect.CloseInputChannels ->
                            AlterChannelsMsg.ChannelType.INPUT
                        else ->
                            AlterChannelsMsg.ChannelType.OUTPUT
                    }
                ))
        }
        is SideEffect.AlterInputChannels -> {
            builder.setModify(
                ModifyChannels.newBuilder()
                    .putAllChannels(
                        this.inputChannels.map {
                            Pair(it.key, "") //FIX
                        }.toMap())
                    .setChannelType(AlterChannelsMsg.ChannelType.INPUT)
                    .setOpType(
                        when(this) {
                            is SideEffect.OpenInputChannels ->
                                AlterChannelsMsg.ModifyChannels.OpType.OPEN
                            else ->
                                AlterChannelsMsg.ModifyChannels.OpType.RESET
                        }
                    )
            )
        }
        is SideEffect.AlterOutputChannels -> {
            builder.setModify(
                ModifyChannels.newBuilder()
                    .putAllChannels(
                        this.outputChannels.map {
                            Pair(it.key, "") //FIX
                        }.toMap())
                    .setChannelType(AlterChannelsMsg.ChannelType.OUTPUT)
                    .setOpType(
                        when(this) {
                            is SideEffect.OpenOutputChannels ->
                                AlterChannelsMsg.ModifyChannels.OpType.OPEN
                            else ->
                                AlterChannelsMsg.ModifyChannels.OpType.RESET
                        }
                    )
            )
        }
        is SideEffect.WriteOnOutputChannels -> {
            builder.setWrite(
                WriteOnOutputChannelMsg.newBuilder()
                    .putAllMessages(this.messages.mapValues {
                        WriteOnOutputChannelMsg.Messages.newBuilder()
                            .addAllMessage(it.value).build()
                    })
            )
        }
        else -> throw ParsingException(this)
    }
    return SideEffectMsg.newBuilder()
        .setChannels(builder).build()
}

fun SideEffect.AlterCustomData.serialize(): SideEffectMsg {
    val builder = AlterCustomDataMsg.newBuilder()
        .putAllData(this.data.map {
            Pair(it.key, it.value.toString())
        }.toMap())
    when(this) {
        is SideEffect.SetPersistentData ->
            builder.setType(AlterCustomDataMsg.OpType.SET_PERSISTENT)
        is SideEffect.SetDurableData ->
            builder.setType(AlterCustomDataMsg.OpType.SET_DURABLE)
        is SideEffect.SetEphemeralData ->
            builder.setType(AlterCustomDataMsg.OpType.SET_EPHEMERAL)
        else -> throw IllegalStateException()
    }
    return SideEffectMsg.newBuilder()
        .setCustomData(builder).build()
}

