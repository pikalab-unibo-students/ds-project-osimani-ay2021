package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.solve.sideffects.SideEffect

data class DistributedResponse(
    val solution: DistributedSolution,
    val sideEffects: List<SideEffect>
) {
    constructor(
        solution: DistributedSolution,
        vararg sideEffects: SideEffect
    ) : this(solution, listOf(*sideEffects))
}