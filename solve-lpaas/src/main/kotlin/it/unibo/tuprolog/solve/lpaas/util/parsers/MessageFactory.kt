package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableClause
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.FlagsMsg.FlagMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperatorSetMsg.OperatorMsg
import it.unibo.tuprolog.theory.Theory


fun fromUnificatorToMsg(unificator: Map<String, String>): UnificatorMsg {
    return UnificatorMsg.newBuilder()
        .addAllSubstitution(unificator.map { SubstitutionMsg.newBuilder()
            .setVar(it.key).setTerm(it.value).build() })
        .build()
}

fun fromLibrariesToMsg(libraries: Set<String>): RuntimeMsg {
    return RuntimeMsg.newBuilder()
        .addAllLibraries(libraries.map { RuntimeMsg.LibraryMsg.newBuilder()
            .setName(it).build() })
        .build()
}

fun fromFlagsToMsg(flags: Map<String, String>): FlagsMsg {
    return FlagsMsg.newBuilder()
        .addAllFlags(flags.map { FlagMsg.newBuilder().setName(it.key)
            .setValue(it.value).build() })
        .build()
}

fun fromOperatorSetToMsg(operators: Map<String, Pair<String, Int>>): OperatorSetMsg {
    return OperatorSetMsg.newBuilder()
        .addAllOperator(operators.map { OperatorMsg.newBuilder().setFunctor(it.key)
            .setSpecifier(it.value.first).setPriority(it.value.second).build() })
        .build()
}

fun fromTheoryToMsg(theory: Theory): TheoryMsg {
    return TheoryMsg.newBuilder()
        .addAllClause(theory.clauses.map { TheoryMsg.ClauseMsg.newBuilder()
            .setContent(it.toString()).build() })
        .build()
}

fun fromChannelsToMsg(channels: Map<String, String>): Channels {
    return Channels.newBuilder()
        .addAllChannel(channels.map { Channels.ChannelID.newBuilder().setName(it.key)
            .setContent(it.value).build() })
        .build()
}

fun fromChannelsToMsg(channels: Set<String>): Channels {
    return Channels.newBuilder()
        .addAllChannel(channels.map { Channels.ChannelID.newBuilder().setName(it).build() })
        .build()
}

fun fromClauseToMutableMsg(solverID: String, struct: Struct): MutableClause {
    return MutableClause.newBuilder()
        .setSolverID(solverID).setClause(TheoryMsg.ClauseMsg.newBuilder().setContent(struct.toString())).build()
}

