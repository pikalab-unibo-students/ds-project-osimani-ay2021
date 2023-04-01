package it.unibo.tuprolog.solve.lpaas.client.trasparent

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.toMap
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

abstract class TrasparentClient: Solver {

    protected abstract val solver: ClientSolver

    override fun solve(goal: Struct, options: SolveOptions): Sequence<Solution> {
        return solver.solve(goal, options).asSequence()
    }

    override val unificator: Unificator
        get() = solver.getUnificator()
    override val libraries: Runtime
        get() = Runtime.of(solver.getLibraries().map { convertStringToKnownLibrary(it) })
    override val flags: FlagStore
        get() = solver.getFlags()
    override val staticKb: Theory
        get() = solver.getStaticKB()
    override val dynamicKb: Theory
        get() = solver.getDynamicKB()
    override val operators: OperatorSet
        get() = solver.getOperators()
    override val inputChannels: InputStore
        get() = InputStore.of(solver.getInputChannels().map {
            Pair(it.first, InputChannel.of(it.second)) }.toMap())
    override val outputChannels: OutputStore
        get() = OutputStore.of(solver.getOutputChannels().map { pair ->
            Pair(pair.first, OutputChannel.of<String> {  println(pair.second) }) }.toMap())
    val solverId: String get() = solver.getId()
}