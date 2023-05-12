package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.solve.Signature

class DistributedPrimitiveWrapper(
    name: String,
    arity: Int,
    val implementation: DistribuitedPrimitive
) {

    val signature: Signature by lazy { Signature(name, arity)}

}