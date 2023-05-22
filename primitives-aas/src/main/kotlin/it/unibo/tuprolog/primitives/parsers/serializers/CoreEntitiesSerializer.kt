package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.messages.StructMsg
import it.unibo.tuprolog.solve.Signature

fun Term.serialize(): ArgumentMsg {
    val builder = ArgumentMsg.newBuilder()
    when(this) {
        is Var -> builder.setVar(this.name)
        is Struct -> builder.setStruct(this.serialize())
        else -> builder.setConstant(this.toString())
    }
    return builder.build()
}

fun Struct.serialize(): StructMsg {
    return StructMsg.newBuilder()
        .setFunctor(this.functor)
        .addAllArguments(
            this.args.map { it.serialize() }
        )
        .build()
}

fun Signature.serialize(): SignatureMsg =
    SignatureMsg.newBuilder().setName(this.name).setArity(this.arity).build()

