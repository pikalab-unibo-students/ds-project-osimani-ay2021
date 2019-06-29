package it.unibo.tuprolog.core

import it.unibo.tuprolog.core.impl.DirectiveImpl

interface Directive : Clause {

    override val head: Struct?
        get() = null

    override val isRule: Boolean
        get() = false

    override val isFact: Boolean
        get() = false

    override val isDirective: Boolean
        get() = true

    companion object {
        fun of(body1: Term, vararg body: Term): Directive =
                DirectiveImpl(Struct.conjunction((listOf(body1) + listOf(*body))))
    }
}
