package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.primitives.messages.CustomDataMsg
import it.unibo.tuprolog.primitives.messages.ExecutionContextMsg
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.data.CustomDataStore

fun ExecutionContext.serialize(): ExecutionContextMsg =
    ExecutionContextMsg.newBuilder()
        .setProcedure(procedure?.serialize())
        .putAllSubstitutions(substitution.map {
            Pair(it.key.name, it.value.serialize())
        }.toMap())
        .addAllLogicStackTrace(logicStackTrace.map { it.serialize()})
        .setCustomDataStore(customData.serialize())
        .putAllUnificator(unificator.context.map {
            Pair(it.key.name, it.value.serialize())
        }.toMap())
        .addAllLibraries(libraries.aliases)
        .putAllFlags(flags.mapValues { it.value.serialize() })
        .addAllStaticKB(staticKb.clauses.map { it.serialize() })
        .addAllDynamicKB(dynamicKb.clauses.map { it.serialize() })
        .addAllOperators(operators.serialize())
        .putAllInputChannels(inputChannels.map { Pair(it.key, "") }.toMap()) //FIX WITH CHANNEL CONTENT
        .addAllOutputChannels(outputChannels.keys)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setRemainingTime(remainingTime)
        .setElapsedTime(elapsedTime)
        .setMaxDuration(maxDuration)
        .build()

fun CustomDataStore.serialize(): CustomDataMsg =
    CustomDataMsg.newBuilder()
        .putAllPersistentData(this.persistent.mapValues { it.value.toString() })
        .putAllDurableData(this.durable.mapValues { it.value.toString() })
        .putAllEphemeralData(this.ephemeral.mapValues { it.value.toString() })
        .build()