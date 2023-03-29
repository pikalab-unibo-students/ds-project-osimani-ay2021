package it.unibo.tuprolog.solve.lpaas.gui

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.exception.TuPrologException
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.theory.Theory
import org.reactfx.EventStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

interface TuPrologIDEModel {

    companion object {
        fun of(executor: ExecutorService = ForkJoinPool.commonPool()): TuPrologIDEModel = TuPrologIDEModelImpl(executor)
    }

    enum class State {
        IDLE, COMPUTING, SOLUTION
    }

    var solveOptions: SolveOptions

    val state: State

    val executor: ExecutorService

    fun newSolver(theory: Theory): String

    fun loadSolver(solverId: String)

    fun closeSolver()

    fun getCurrentSolver(): ClientMutableSolver?

    fun customizeSolver(customizer: (ClientMutableSolver) -> ClientMutableSolver)

    fun setStdin(content: String)

    fun quit()

    fun solve()

    fun solveAll()

    fun next()

    fun nextAll()

    fun stop()

    fun reset()

    var query: String

//    var goal: Struct

    val onReset: EventStream<SolverEvent<Unit>>

    val onQuit: EventStream<Unit>

    val onSolveOptionsChanged: EventStream<SolveOptions>

    val onSolverLoaded: EventStream<SolverEvent<Unit>>

    val onSolverClosed: EventStream<String>

    val onQueryChanged: EventStream<String>

    val onNewSolver: EventStream<SolverEvent<Unit>>

    val onNewQuery: EventStream<SolverEvent<Struct>>

    val onResolutionStarted: EventStream<SolverEvent<Int>>

    val onNewSolution: EventStream<SolverEvent<Solution>>

    val onResolutionOver: EventStream<SolverEvent<Int>>

    val onQueryOver: EventStream<SolverEvent<Struct>>

    val onStdoutPrinted: EventStream<String>

    val onStderrPrinted: EventStream<String>

    val onWarning: EventStream<Warning>

    val onError: EventStream<TuPrologException>
}
