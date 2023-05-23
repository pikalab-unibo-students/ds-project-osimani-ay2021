package it.unibo.tuprolog.primitives.server.distribuited.solve

import it.unibo.tuprolog.primitives.server.distribuited.DistributedSolution
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