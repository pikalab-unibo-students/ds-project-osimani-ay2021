package it.unibo.tuprolog.theory.impl

import it.unibo.tuprolog.collections.ClauseQueue
import it.unibo.tuprolog.collections.RetrieveResult
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.theory.AbstractTheory
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.TheoryUtils.checkClausesCorrect

internal class IndexedTheory private constructor(private val queue: ClauseQueue) : AbstractTheory() {

    /** Construct a Clause database from given clauses */
    constructor(clauses: Iterable<Clause>) : this(ClauseQueue.of(clauses)) {
        checkClausesCorrect(clauses)
    }

    override val clauses: Iterable<Clause> by lazy { queue.toList() }

    override fun plus(theory: Theory): Theory =
        IndexedTheory(
            clauses + checkClausesCorrect(
                theory.clauses
            )
        )

    override fun get(clause: Clause): Sequence<Clause> = queue[clause]

    override fun assertA(clause: Clause): Theory =
        IndexedTheory(
            ClauseQueue.of(listOf(clause) + clauses)
        )

    override fun assertZ(clause: Clause): Theory =
        IndexedTheory(
            ClauseQueue.of(clauses + clause)
        )

    override fun retract(clause: Clause): RetractResult {
        val newTheory = ClauseQueue.of(clauses)
        val retracted = newTheory.retrieveFirst(clause)

        return when (retracted) {
            is RetrieveResult.Failure ->
                RetractResult.Failure(this)
            else -> RetractResult.Success(
                IndexedTheory(retracted.collection),
                (retracted as RetrieveResult.Success).clauses
            )
        }
    }

    override fun retractAll(clause: Clause): RetractResult {
        val newTheory = ClauseQueue.of(clauses)
        val retracted = newTheory.retrieveAll(clause)

        return when (retracted) {
            is RetrieveResult.Failure -> RetractResult.Failure(this)
            else -> RetractResult.Success(
                IndexedTheory(retracted.collection),
                (retracted as RetrieveResult.Success).clauses
            )
        }
    }
}