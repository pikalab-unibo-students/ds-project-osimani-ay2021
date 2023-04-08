package it.unibo.tuprolog.solve.lpaas.util.parsers

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermDeserializer
import it.unibo.tuprolog.serialize.TermSerializer
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.RuntimeMsg.LibraryMsg
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parse
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serialize
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

object SolverDeserializer {
    val deserializer = TermDeserializer.of(MimeType.Json)

    /** Unificator */
    fun UnificatorMsg.parse(): Unificator =
        this.substitutionList.parse()

    fun List<SubstitutionMsg>.parse(): Unificator {
        val scope = Scope.empty()
        return Unificator.strict(Substitution.Companion.of(
            this.associate { Pair(scope.varOf(it.`var`), deserializer.deserialize(it.term)) }))
    }

    /** Runtime */
    fun LibraryMsg.parse(): Library =
        convertStringToKnownLibrary(this.name)

    fun RuntimeMsg.parse(): Runtime =
        this.librariesList.parse()

    fun List<LibraryMsg>.parse(): Runtime =
        Runtime.of(this.map {
            it.parse()
        })

    /** FlagStore */
    fun FlagsMsg.parse(): FlagStore =
        this.flagsList.parse()

    fun FlagsMsg.FlagMsg.parse(): Pair<String, Term> =
        Pair(this.name, deserializer.deserialize(this.value))

    fun List<FlagsMsg.FlagMsg>.parse(): FlagStore {
        return FlagStore.of(this.associate { it.parse() })
    }

    /** Theory */
    fun TheoryMsg.ClauseMsg.parseToStruct(): Struct =
        deserializer.deserialize(this.content).castToStruct()

    fun TheoryMsg.ClauseMsg.parseToClause(): Clause =
        deserializer.deserialize(this.content).castToClause()

    fun TheoryMsg.parse(): Theory =
        this.clauseList.parse()

    fun List<TheoryMsg.ClauseMsg>.parse(): Theory =
        Theory.of( this.map { it.parseToClause() })
}