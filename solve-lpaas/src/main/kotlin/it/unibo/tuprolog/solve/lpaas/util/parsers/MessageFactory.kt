package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermDeserializer
import it.unibo.tuprolog.serialize.TermSerializer
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableClause
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableLibrary
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels.ChannelID
import it.unibo.tuprolog.solve.lpaas.solveMessage.FlagsMsg.FlagMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperatorSetMsg.OperatorMsg
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

/** Basic messages **/

val deserializer = TermDeserializer.of(MimeType.Json)
val serializer = TermSerializer.of(MimeType.Json)

fun fromUnificatorToMsg(unificator: Unificator): UnificatorMsg {
    return UnificatorMsg.newBuilder()
        .addAllSubstitution(unificator.context.map { SubstitutionMsg.newBuilder()
            .setVar(serializer.serialize(it.key))
            .setTerm(serializer.serialize(it.value)).build() })
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

fun fromFlagToMsg(key: String, value: Term): FlagsMsg.FlagMsg {
    return FlagMsg.newBuilder().setName(key)
            .setValue(serializer.serialize(value)).build()
}

fun fromFlagsToMsg(flags: FlagStore): FlagsMsg {
    return FlagsMsg.newBuilder()
        .addAllFlags(flags.map { fromFlagToMsg(it.key, it.value) })
        .build()
}

fun fromClauseToMsg(struct: Clause): TheoryMsg.ClauseMsg {
    return TheoryMsg.ClauseMsg.newBuilder()
        .setContent(serializer.serialize(struct)).build()
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
        .setSolverID(solverID).setClause(TheoryMsg.ClauseMsg.newBuilder()
            .setContent(serializer.serialize(struct))).build()
}

fun fromLibraryToMutableMsg(solverID: String, libraryName: String): MutableLibrary {
    return MutableLibrary.newBuilder().setSolverID(solverID)
        .setLibrary(fromLibraryToMsg(libraryName)).build()
}

fun fromLineEventToMsg(solverID: String = "", channelID: String = "", content: String): LineEvent {
    return LineEvent.newBuilder().setLine(content)
        .setSolverID(solverID).setChannelID(fromChannelIDToMsg(channelID)).build()
}

