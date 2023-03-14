package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableClause
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableLibrary
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels.ChannelID
import it.unibo.tuprolog.solve.lpaas.solveMessage.FlagsMsg.FlagMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperatorSetMsg.OperatorMsg
import it.unibo.tuprolog.theory.Theory

/** Basic messages **/

fun fromUnificatorToMsg(unificator: Map<String, String>): UnificatorMsg {
    return UnificatorMsg.newBuilder()
        .addAllSubstitution(unificator.map { SubstitutionMsg.newBuilder()
            .setVar(it.key).setTerm(it.value).build() })
        .build()
}

fun fromLibraryToMsg(library: String): RuntimeMsg.LibraryMsg {
    return RuntimeMsg.LibraryMsg.newBuilder()
            .setName(library).build()
}

fun fromLibrariesToMsg(libraries: Set<String>): RuntimeMsg {
    return RuntimeMsg.newBuilder()
        .addAllLibraries(libraries.map { fromLibraryToMsg(it) })
        .build()
}

fun fromFlagToMsg(key: String, value: String): FlagsMsg.FlagMsg {
    return FlagMsg.newBuilder().setName(key)
            .setValue(value).build()
}

fun fromFlagsToMsg(flags: Map<String, String>): FlagsMsg {
    return FlagsMsg.newBuilder()
        .addAllFlags(flags.map { fromFlagToMsg(it.key, it.value) })
        .build()
}

fun fromOperatorSetToMsg(operators: Map<String, Pair<String, Int>>): OperatorSetMsg {
    return OperatorSetMsg.newBuilder()
        .addAllOperator(operators.map { OperatorMsg.newBuilder().setFunctor(it.key)
            .setSpecifier(it.value.first).setPriority(it.value.second).build() })
        .build()
}

fun fromClauseToMsg(struct: Struct): TheoryMsg.ClauseMsg {
    return TheoryMsg.ClauseMsg.newBuilder()
        .setContent(struct.toString()).build()
}

fun fromTheoryToMsg(theory: Theory): TheoryMsg {
    return TheoryMsg.newBuilder()
        .addAllClause(theory.clauses.map { fromClauseToMsg(it) })
        .build()
}

fun fromChannelIDToMsg(name: String, content: String = ""): Channels.ChannelID {
    return ChannelID.newBuilder().setName(name)
            .setContent(content).build()
}

fun fromChannelsToMsg(channels: Map<String, String>): Channels {
    return Channels.newBuilder()
        .addAllChannel(channels.map { fromChannelIDToMsg(it.key, it.value) })
        .build()
}

fun fromChannelsToMsg(channels: Set<String>): Channels {
    return Channels.newBuilder()
        .addAllChannel(channels.map { fromChannelIDToMsg(it) })
        .build()
}

/** Mutable Messages **/

fun fromClauseToMutableMsg(solverID: String, struct: Struct): MutableClause {
    return MutableClause.newBuilder()
        .setSolverID(solverID).setClause(TheoryMsg.ClauseMsg.newBuilder().setContent(struct.toString())).build()
}

fun fromLibraryToMutableMsg(solverID: String, libraryName: String): MutableLibrary {
    return MutableLibrary.newBuilder().setSolverID(solverID)
        .setLibrary(fromLibraryToMsg(libraryName)).build()
}

fun fromLineEventToMsg(solverID: String = "", channelID: String = "", content: String): LineEvent {
    return LineEvent.newBuilder().setLine(content)
        .setSolverID(solverID).setChannelID(fromChannelIDToMsg(channelID)).build()
}

