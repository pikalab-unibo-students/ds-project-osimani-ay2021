package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermDeserializer
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableClause
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableLibrary
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels.ChannelID
import it.unibo.tuprolog.solve.lpaas.solveMessage.ReadLine
import it.unibo.tuprolog.solve.lpaas.solveMessage.RuntimeMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.RuntimeMsg.LibraryMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.SolutionReply.CustomDataMsg
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toMsg
import it.unibo.tuprolog.theory.Theory

/** Basic messages **/

object MessageBuilder {

    fun fromChannelIDToMsg(name: String, content: List<String> = emptyList()): ChannelID {
        return ChannelID.newBuilder().setName(name)
            .addAllContent(content).build()
    }

    fun fromChannelsToMsg(channels: Map<String, String>): Channels {
        return Channels.newBuilder()
            .addAllChannel(channels.map {
                fromChannelIDToMsg(it.key, it.value.map { it.toString() }) })
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
            .setSolverID(solverID).setClause(
                TheoryMsg.ClauseMsg.newBuilder()
                    .setContent(serializer.serialize(struct))
            ).build()
    }

    fun fromLibraryToMutableMsg(solverID: String, library: String): MutableLibrary {
        return MutableLibrary.newBuilder().setSolverID(solverID)
            .setLibrary(LibraryMsg.newBuilder().setName(library).build()).build()
    }

    fun fromLibrariesToMsg(libraries: Set<String>): RuntimeMsg {
        return RuntimeMsg.newBuilder().addAllLibraries(
            libraries.map { LibraryMsg.newBuilder().setName(it).build()}).build()
    }

    fun fromReadLineToMsg(content: String): ReadLine {
        return ReadLine.newBuilder().setLine(content).build()
    }

    fun fromCustomDataStoreToMsg(customStore: CustomDataStore): List<CustomDataMsg> {
        return customStore.persistent.map {
            CustomDataMsg.newBuilder().setName(it.key).setValue(it.value.toString()).build()
        }
    }
}

