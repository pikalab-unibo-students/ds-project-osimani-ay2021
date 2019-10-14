package it.unibo.tuprolog.core.operators

import it.unibo.tuprolog.core.*

/** Class representing a Prolog Operator */
class Operator(val functor: String, val associativity: Associativity, val priority: Int) : Comparable<Operator> {

    override fun compareTo(other: Operator): Int =
            when {
                priority > other.priority -> 1
                priority < other.priority -> -1
                else -> associativity.compareTo(other.associativity).let { associativityCompareTo ->
                    when (associativityCompareTo) {
                        0 -> functor.compareTo(other.functor)
                        else -> associativityCompareTo
                    }
                }
            }

    /** Creates a Term from this operator */
    fun toTerm(): Struct =
            Struct.of(FUNCTOR, priority.toTerm(), associativity.toTerm(), functor.toAtom())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Operator

        if (functor != other.functor) return false
        if (associativity != other.associativity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = functor.hashCode()
        result = 31 * result + associativity.hashCode()
        return result
    }

    override fun toString(): String = "Operator($priority, $associativity, '$functor')"

    companion object {

        /** The Operator functor */
        const val FUNCTOR = "op"

        /** An operator template */
        val TEMPLATE = Struct.of(FUNCTOR, Var.of("P"), Var.of("A"), Var.of("F"))

        /** Creates an Operator instance from a well-formed Struct, or returns `null` if it cannot be interpreted as Operator */
        fun fromTerm(struct: Struct): Operator? = with(struct) {
            when {
                functor == FUNCTOR && arity == 3 &&
                        args[0] is Integer && args[1] is Atom && args[2] is Atom -> try {

                    Operator(
                            args[2].`as`<Atom>().value,
                            Associativity.fromTerm(args[1]),
                            args[0].`as`<Numeric>().intValue.toInt()
                    )

                } catch (ex: IllegalArgumentException) {
                    null
                }

                else -> null
            }
        }
    }
}