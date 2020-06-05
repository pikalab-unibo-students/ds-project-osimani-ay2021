package it.unibo.tuprolog.solve.stdlib.primitive

import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper

/** A base class to implement predicates with zero argument */
abstract class PredicateWithoutArguments<E : ExecutionContext>(operator: String) : PrimitiveWrapper<E>(operator, 0)