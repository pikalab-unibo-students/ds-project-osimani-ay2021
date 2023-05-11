package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.sideffects.SideEffect

data class DistributedResponse(
    val solution: Solution,
    val sideEffects: List<SideEffect>
) {
    constructor(
        solution: Solution,
        vararg sideEffects: SideEffect
    ) : this(solution, listOf(*sideEffects))
}