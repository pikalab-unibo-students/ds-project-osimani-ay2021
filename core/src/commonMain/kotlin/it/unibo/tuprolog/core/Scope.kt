package it.unibo.tuprolog.core

import it.unibo.tuprolog.core.impl.ScopeImpl
import org.gciatto.kt.math.BigDecimal
import org.gciatto.kt.math.BigInteger
import it.unibo.tuprolog.core.List as LogicList
import it.unibo.tuprolog.core.Set as LogicSet

interface Scope {

    val variables: Map<String, Var>

    operator fun contains(variable: Var): Boolean

    operator fun contains(variable: String): Boolean

    operator fun get(variable: String): Var?

    fun where(lambda: Scope.() -> Unit): Scope

    fun <R> with(lambda: Scope.() -> R): R

    fun varOf(name: String): Var

    fun atomOf(value: String): Atom

    fun structOf(functor: String, vararg args: Term): Struct

    fun structOf(functor: String, args: Sequence<Term>): Struct

    fun tupleOf(vararg terms: Term): Tuple

    fun tupleOf(terms: Iterable<Term>): Tuple

    fun listOf(vararg terms: Term): LogicList

    fun emptyList(): EmptyList

    fun emptySet(): EmptySet

    fun listOf(terms: Iterable<Term>): LogicList

    fun listFrom(terms: Iterable<Term>, last: Term? = null): LogicList

    fun setOf(vararg terms: Term): LogicSet

    fun setOf(terms: Iterable<Term>): LogicSet

    fun factOf(head: Struct): Fact

    fun ruleOf(head: Struct, body1: Term, vararg body: Term): Rule

    fun directiveOf(body1: Term, vararg body: Term): Directive

    fun clauseOf(head: Struct?, vararg body: Term): Clause

    fun consOf(head: Term, tail: Term): Cons

    fun indicatorOf(name: Term, arity: Term): Indicator

    fun indicatorOf(name: String, arity: Int): Indicator

    fun anonymous(): Var

    @Suppress("PropertyName")
    val `_`: Var
        get() = anonymous()

    fun whatever(): Var

    fun numOf(value: BigDecimal): Real

    fun numOf(value: Double): Real

    fun numOf(value: Float): Real

    fun numOf(value: BigInteger): Integer

    fun numOf(value: Int): Integer

    fun numOf(value: Long): Integer

    fun numOf(value: Short): Integer

    fun numOf(value: Byte): Integer

    fun numOf(value: String): Numeric

    fun truthOf(value: Boolean): Truth

    companion object {

        fun empty(): Scope = ScopeImpl(mutableMapOf())

        fun of(vararg vars: String): Scope = of(*vars) {}

        fun of(vararg vars: String, lambda: Scope.() -> Unit): Scope =
            of(*vars.map { Var.of(it) }.toTypedArray(), lambda = lambda)

        fun of(vararg vars: Var): Scope = of(*vars) {}

        fun of(vararg vars: Var, lambda: Scope.() -> Unit): Scope =
            ScopeImpl(vars.map { it.name to it }.toMap(mutableMapOf()))
                .where(lambda)


        fun <R> empty(lambda: Scope.() -> R): R = empty().with(lambda)

        fun <R> of(vararg vars: String, lambda: Scope.() -> R): R = of(*vars).with(lambda)

        fun <R> of(vararg vars: Var, lambda: Scope.() -> R): R = of(*vars).with(lambda)
    }
}