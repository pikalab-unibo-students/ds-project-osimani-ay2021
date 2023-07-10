package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.primitives.messages.*
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

fun ArgumentMsg.deserialize(scope: Scope = Scope.empty()): Term =
    if(this.hasVar()) {
        deserializeVar(this.`var`, scope)
    } else if(this.hasStruct()) {
        this.struct.deserialize()
    } else if(this.hasFlag()) {
        Truth.of(this.flag)
    } else if(this.hasNumeric()) {
        val value = this.numeric
        if(value % 1 > 0)
            Constant.parse(this.numeric.toString())
        else
            Constant.parse(this.numeric.toInt().toString())
    } else {
        Atom.parse(this.atom)
    }

fun deserializeVar(name: String, scope: Scope = Scope.empty()): Var =
    scope.varOf(name)

fun StructMsg.deserialize(scope: Scope = Scope.empty()): Struct =
    Struct.of(this.functor, this.argumentsList.map { it.deserialize(scope) })

fun StructMsg.deserializeAsClause(scope: Scope = Scope.empty()): Clause? =
    if(this != StructMsg.getDefaultInstance())
        this.deserialize(scope).asClause()
    else
        null

fun SignatureMsg.deserialize(): Signature = Signature(this.name, this.arity)

fun OperatorMsg.deserialize(): Operator =
    Operator(this.functor, Specifier.valueOf(this.specifier), this.priority)

fun CustomDataMsg.deserialize(): CustomDataStore = CustomDataStore.empty().copy(
    this.persistentDataMap,
    this.durableDataMap,
    this.ephemeralDataMap
)

fun UnificatorMsg.deserialize(scope: Scope = Scope.empty()): Unificator =
    Unificator.naive(
        Substitution.of(this.unificatorMap.map {
            Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
        }.toMap()))

fun LogicStacktraceMsg.deserialize(scope: Scope = Scope.empty()): List<Struct> =
    this.logicStackTraceList.map {
        it.deserialize(scope)
    }

fun FlagsMsg.deserialize(): FlagStore =
    FlagStore.of(
        this.flagsMap.map {
            Pair(it.key, it.value.deserialize())
        }.toMap())

fun TheoryMsg.deserialize(): Theory =
    Theory.of(this.clausesList.map {
        it.deserialize().castToClause()
    })

fun OperatorSetMsg.deserialize(): OperatorSet =
    OperatorSet(this.operatorsList.map {
        it.deserialize()
    })

fun LibrariesMsg.deserialize(): DistributedRuntime =
    DistributedRuntime.of(
        this.librariesList.map { it.deserialize() }
    )

fun LibraryMsg.deserialize(): DistributedRuntime.DistributedLibrary =
    DistributedRuntime.DistributedLibrary(
        this.alias,
        this.primitivesList.map { it.deserialize() }.toSet(),
        this.rulesSignaturesList.map { it.deserialize() }.toSet(),
        this.clausesList.map { it.deserialize().asClause()!! }.toSet(),
        this.functionsSignaturesList.map { it.deserialize() }.toSet()
    )

