package it.unibo.tuprolog.core.impl

import it.unibo.tuprolog.core.Cons
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.utils.cursor

internal class ConsImpl(
    override val head: Term,
    override val tail: Term,
    tags: Map<String, Any> = emptyMap()
) : AbstractCons(listOf(head, tail), tags), Cons {

    override val isGround: Boolean = checkGroundness()

    override val last: Term = when {
        tail.isList -> tail.castToList().last
        else -> tail
    }

    override fun checkGroundness(): Boolean = head.isGround && tail.isGround

    override val estimatedLength: Int = 1 + (tail.asList()?.estimatedLength ?: 1)

    override fun applyNonEmptyUnifier(unifier: Substitution.Unifier): Term =
        if (estimatedLength >= SWITCH_TO_LAZY_THRESHOLD) {
            LazyConsWithImplicitLast(unfoldedSequence.cursor().map { it.apply(unifier) }, tags)
        } else {
            ConsImpl(head.apply(unifier), tail.apply(unifier), tags)
        }

    override fun copyWithTags(tags: Map<String, Any>): ConsImpl =
        if (this.tags === tags) this else ConsImpl(head, tail, tags)
}
