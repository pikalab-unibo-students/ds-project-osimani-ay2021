package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

sealed interface DistributedSolution {

    /** The query to which the solution refers */
    val query: Struct

    /** The substitution that has been applied to find the solution, or a failed substitution */
    val substitution: Substitution

    val exception: DistributedError?

    /** The [Struct] representing the solution, or `null` in case of a non-successful solution */
    val solvedQuery: Struct?

    val isYes: Boolean

    val isNo: Boolean

    val isHalt: Boolean

    class Yes(
        override val query: Struct,
        override val substitution: Substitution = Substitution.empty()
    ): DistributedSolution {
        override val exception: DistributedError? = null

        override val solvedQuery: Struct? = substitution.applyTo(query)?.castToStruct()

        override val isYes: Boolean = true
        override val isNo: Boolean = false
        override val isHalt: Boolean = false
    }

    /** A type representing a failed solution */
    class No(
        override val query: Struct
    ): DistributedSolution {
        override val substitution: Substitution = Substitution.failed()

        override val exception: DistributedError? = null

        override val solvedQuery: Struct? = substitution.applyTo(query)?.castToStruct()

        override val isYes: Boolean = false
        override val isNo: Boolean = true
        override val isHalt: Boolean = false
    }

    /** A type representing a failed (halted) solution because of an exception */
    class Halt(
        override val query: Struct,
        error: DistributedError
    ): DistributedSolution {
        override val substitution: Substitution = Substitution.failed()

        override val exception: DistributedError = error

        override val solvedQuery: Struct? = substitution.applyTo(query)?.castToStruct()

        override val isYes: Boolean = false
        override val isNo: Boolean = false
        override val isHalt: Boolean = true
    }

    companion object {
        fun yes(
            query: Struct,
            substitution: Substitution = Substitution.empty()
        ): DistributedSolution =
            Yes(query, substitution)

        fun yes(
            query: Struct
        ): DistributedSolution =
            Yes(query)

        fun no(
            query: Struct,
        ): DistributedSolution =
            No(query)

        fun halt(
            query: Struct,
            error: DistributedError
        ): DistributedSolution =
            Halt(query, error)

    }

}