package it.unibo.tuprolog.solve.library.stdlib.rule

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.library.stdlib.primitive.EnsureExecutable
import it.unibo.tuprolog.solve.rule.RuleWrapper
import it.unibo.tuprolog.solve.ExecutionContextImpl

import kotlin.collections.List as KtList
import kotlin.collections.listOf as ktListOf

object Call : RuleWrapper<ExecutionContextImpl>("call", 1) {
    override val Scope.head: KtList<Term>
        get() = ktListOf(varOf("G"))

    override val Scope.body: Term
        get() = tupleOf(
            structOf(EnsureExecutable.functor, varOf("G")),
            varOf("G")
        )
}