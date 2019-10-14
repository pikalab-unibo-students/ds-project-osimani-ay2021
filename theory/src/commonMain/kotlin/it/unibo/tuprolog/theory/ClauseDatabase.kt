package it.unibo.tuprolog.theory

import it.unibo.tuprolog.core.*

interface ClauseDatabase : Iterable<Clause> {

    /** All [Clause]s in this database */
    val clauses: Iterable<Clause>

    /** Only [clauses] that are [Rule]s */
    val rules: Iterable<Rule>
        get() = clauses.filterIsInstance<Rule>()

    /** Only [clauses] that are [Directive]s */
    val directives: Iterable<Directive>
        get() = clauses.filterIsInstance<Directive>()

    /** Adds given ClauseDatabase to this */
    operator fun plus(clauseDatabase: ClauseDatabase): ClauseDatabase

    /** Adds given Clause to this ClauseDatabase */
    operator fun plus(clause: Clause): ClauseDatabase = assertZ(clause)

    /** Checks if given clause is contained in this database */
    operator fun contains(clause: Clause): Boolean

    /** Checks if given clause is present in this database */
    operator fun contains(head: Struct): Boolean

    /** Checks if clauses exists in this database having the specified [Indicator] as head; this should be [well-formed][Indicator.isWellFormed] */
    operator fun contains(indicator: Indicator): Boolean

    /** Retrieves matching clauses from this database */
    operator fun get(clause: Clause): Sequence<Clause>

    /** Retrieves matching rules from this database */
    operator fun get(head: Struct): Sequence<Rule>

    /** Retrieves all rules in this database having the specified [Indicator] as head; this should be [well-formed][Indicator.isWellFormed] */
    operator fun get(indicator: Indicator): Sequence<Rule>

    /** Adds given clause before all other clauses in this database */
    fun assertA(clause: Clause): ClauseDatabase

    /** Adds given clause before all other clauses in this database */
    fun assertA(struct: Struct): ClauseDatabase = assertA(Fact.of(struct))

    /** Adds given clause after all other clauses in this database */
    fun assertZ(clause: Clause): ClauseDatabase

    /** Adds given clause after all other clauses in this database */
    fun assertZ(struct: Struct): ClauseDatabase = assertZ(Fact.of(struct))

    /** Tries to delete a matching clause from this database */
    fun retract(clause: Clause): RetractResult

    /** Tries to delete a matching clause from this database */
    fun retract(head: Struct): RetractResult = retract(Rule.of(head, Var.anonymous()))

    /** Tries to delete all matching clauses from this database */
    fun retractAll(clause: Clause): RetractResult

    /** Tries to delete all matching clauses from this database */
    fun retractAll(head: Struct): RetractResult = retractAll(Rule.of(head, Var.anonymous()))

    companion object {

        /** Creates an empty ClauseDatabase */
        fun empty(): ClauseDatabase = of(emptySequence())

        /** Creates a ClauseDatabase with given clauses */
        fun of(vararg clause: Clause): ClauseDatabase = of(clause.asIterable())

        /** Let developers easily create a ClauseDatabase programmatically while avoiding variables names clashing */
        fun of(vararg clause: Scope.() -> Clause): ClauseDatabase = of(clause.map { Scope.empty(it) })

        /** Creates a ClauseDatabase with given clauses */
        fun of(clauses: Sequence<Clause>): ClauseDatabase = of(clauses.asIterable())

        /** Creates a ClauseDatabase with given clauses */
        fun of(clauses: Iterable<Clause>): ClauseDatabase = ClauseDatabaseImpl(clauses)
    }
}