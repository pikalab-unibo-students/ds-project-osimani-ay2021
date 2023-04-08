package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermSerializer
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.RuntimeMsg.LibraryMsg
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serialize
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toFlagMsg
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toMsg
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

object SolverSerializer {

    val serializer = TermSerializer.of(MimeType.Json)

    /** Unificator */
    fun Unificator.serialize(): Map<String, String> =
        this.context.map { Pair(serializer.serialize(it.key), serializer.serialize(it.value)) }.toMap()

    fun Unificator.toMsg(): UnificatorMsg = UnificatorMsg.newBuilder()
        .addAllSubstitution(this.serialize().map {
            SubstitutionMsg.newBuilder()
                .setVar(it.key)
                .setTerm(it.value).build()
        })
        .build()

    /** FlagStore */
    private fun Map.Entry<String, Term>.toFlagMsg(): FlagsMsg.FlagMsg =
        Pair(this.key, this.value).toFlagMsg()

    fun Pair<String, Term>.toFlagMsg(): FlagsMsg.FlagMsg =
        FlagsMsg.FlagMsg.newBuilder().setName(this.first)
            .setValue(serializer.serialize(this.second)).build()

    fun FlagStore.serialize(): Map<String, String> =
        this.map { Pair(it.key, serializer.serialize(it.value)) }.toMap()

    fun FlagStore.toMsg(): FlagsMsg =
        FlagsMsg.newBuilder()
            .addAllFlags(this.map { it.toFlagMsg() })
            .build()

    /** Runtime */
    fun Library.serialize(): String =
        this.alias

    fun Library.toMsg(): LibraryMsg =
        LibraryMsg.newBuilder()
            .setName(this.serialize()).build()

    fun Runtime.serialize(): Set<String> =
        this.map { it.value.serialize() }.toSet()

    fun Runtime.toMsg(): RuntimeMsg =
        RuntimeMsg.newBuilder()
            .addAllLibraries(this.libraries.map { it.toMsg() })
            .build()

    /** Theory */

    fun Struct.serialize(): String =
        serializer.serialize(this)

    fun Clause.toMsg(): TheoryMsg.ClauseMsg =
        TheoryMsg.ClauseMsg.newBuilder()
            .setContent(this.serialize()).build()

    fun Theory.serialize(): List<String> =
        this.clauses.map { it.serialize() }

    fun Theory.toMsg(): TheoryMsg =
        TheoryMsg.newBuilder()
            .addAllClause(this.clauses.map { it.toMsg() })
            .build()

    fun InputStore.serialize(): Set<String> =
        this.map { it.key }.toSet()

    /** OperatorSet */
    fun OperatorSet.toMsg(): OperatorSetMsg {
        val messageBuilder = OperatorSetMsg.newBuilder()
        this.forEach {
            messageBuilder.addOperator(OperatorSetMsg.OperatorMsg.newBuilder()
                .setFunctor(it.functor).setSpecifier(it.specifier.name).setPriority(it.priority))
        }
        return messageBuilder.build()
    }

    /** Channels */

}