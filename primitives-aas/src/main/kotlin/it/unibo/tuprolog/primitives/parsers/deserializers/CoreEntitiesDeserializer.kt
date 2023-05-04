package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.messages.OperatorMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.messages.StructMsg
import it.unibo.tuprolog.solve.Signature

fun ArgumentMsg.deserialize(scope: Scope = Scope.empty()): Term =
    if(this.hasVar()) {
        deserializeVar(this.`var`, scope)
    } else if(this.hasStruct()) {
        this.struct.deserialize()
    } else {
        Term.parse(this.constant)
    }

fun deserializeVar(name: String, scope: Scope = Scope.empty()): Var =
    scope.varOf(name)

fun StructMsg.deserialize(scope: Scope = Scope.empty()): Struct =
    Struct.of(this.functor, this.argumentsList.map { it.deserialize(scope) })

fun SignatureMsg.deserialize(): Signature = Signature(this.name, this.arity)

fun OperatorMsg.deserialize(): Operator =
    Operator(this.functor, Specifier.valueOf(this.specifier), this.priority)

