package it.unibo.tuprolog.solve.libs.oop.primitives

import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.error.TypeError
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import it.unibo.tuprolog.solve.libs.oop.Ref
import it.unibo.tuprolog.solve.libs.oop.TypeRef
import it.unibo.tuprolog.solve.primitive.Solve

fun <C : ExecutionContext> Solve.Request<C>.ensuringArgumentIsRef(index: Int): Solve.Request<C> =
    when (val arg = arguments[index]) {
        !is Ref -> throw TypeError.forArgument(context, signature, TypeError.Expected.REFERENCE, arg, index)
        else -> this
    }

fun <C : ExecutionContext> Solve.Request<C>.ensuringArgumentIsObjectRef(index: Int): Solve.Request<C> =
    when (val arg = arguments[index]) {
        !is ObjectRef -> throw TypeError.forArgument(context, signature, TypeError.Expected.OBJECT_REFERENCE, arg, index)
        else -> this
    }

fun <C : ExecutionContext> Solve.Request<C>.ensuringArgumentIsTypeRef(index: Int): Solve.Request<C> =
    when (val arg = arguments[index]) {
        !is TypeRef -> throw TypeError.forArgument(context, signature, TypeError.Expected.TYPE_REFERENCE, arg, index)
        else -> this
    }
