@file:Suppress("NOTHING_TO_INLINE")

package it.unibo.tuprolog.core.impl

import it.unibo.tuprolog.core.Term

internal actual inline fun isTerm(any: Any?): Boolean = any is Term

internal actual inline fun asTerm(any: Any?): Term? = any as? Term
