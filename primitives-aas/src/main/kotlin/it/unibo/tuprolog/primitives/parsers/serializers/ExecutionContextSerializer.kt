package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.messages.*
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

fun ExecutionContext.serialize(): ExecutionContextMsg =
    ExecutionContextMsg.newBuilder()
        .setProcedure(procedure?.serialize())
        .putAllSubstitutions(substitution.map {
            Pair(it.key.name, it.value.serialize())
        }.toMap())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setRemainingTime(remainingTime)
        .setElapsedTime(elapsedTime)
        .setMaxDuration(maxDuration)
        .build()

fun Unificator.serialize(): UnificatorMsg =
    UnificatorMsg.newBuilder()
        .putAllUnificator(this.context.map {
            Pair(it.key.name, it.value.serialize())}.toMap())
        .build()

fun Runtime.serialize(): LibrariesMsg =
    LibrariesMsg.newBuilder()
        .addAllLibraries(this.aliases).build()

fun FlagStore.serialize(): FlagsMsg =
    FlagsMsg.newBuilder()
        .putAllFlags(this.mapValues { it.value.serialize() })
        .build()

fun Theory.serialize(): TheoryMsg =
    TheoryMsg.newBuilder()
        .addAllClauses(this.clauses.map { it.serialize() })
        .build()

fun OperatorSet.serialize(): OperatorSetMsg =
    OperatorSetMsg.newBuilder()
        .addAllOperators(
            this.map {
                OperatorMsg.newBuilder()
                    .setFunctor(it.functor)
                    .setPriority(it.priority)
                    .setSpecifier(it.specifier.name)
                    .build()
            }).build()

fun CustomDataStore.serialize(): CustomDataMsg =
    CustomDataMsg.newBuilder()
        .putAllPersistentData(this.persistent.mapValues { it.value.toString() })
        .putAllDurableData(this.durable.mapValues { it.value.toString() })
        .putAllEphemeralData(this.ephemeral.mapValues { it.value.toString() })
        .build()