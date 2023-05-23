package it.unibo.tuprolog.primitives.server.distribuited.solve

import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitive
import it.unibo.tuprolog.solve.Signature

class DistributedPrimitiveWrapper(
    name: String,
    arity: Int,
    val implementation: DistributedPrimitive
) {

    val signature: Signature by lazy { Signature(name, arity)}

}