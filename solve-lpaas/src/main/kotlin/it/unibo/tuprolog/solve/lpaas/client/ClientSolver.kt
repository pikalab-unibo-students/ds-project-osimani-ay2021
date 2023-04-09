package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.prolog.SolutionsSequence
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import java.util.concurrent.BlockingDeque

interface ClientSolver {

    fun solve(goal: Struct, options: SolveOptions): SolutionsSequence

    fun solve(goal: String, options: SolveOptions): SolutionsSequence =
        solve(Struct.parse(goal), options)
    fun solve(goal: String): SolutionsSequence =
        solve(goal, SolveOptions.DEFAULT)
    fun solve(goal: String, timeout: TimeDuration): SolutionsSequence =
        solve(goal, SolveOptions.allLazilyWithTimeout(timeout))
    fun solveList(goal: String, timeout: TimeDuration): List<Solution> =
        solveList(goal, SolveOptions.allLazilyWithTimeout(timeout))
    fun solveList(goal: String): List<Solution> =
        solveList(goal, SolveOptions.DEFAULT)
    fun solveList(goal: String, options: SolveOptions): List<Solution> =
        solve(goal, options).asSequence().toList()
    fun solveOnce(goal: String): Solution =
        solveOnce(goal, SolveOptions.DEFAULT)
    fun solveOnce(goal: String, timeout: TimeDuration): Solution =
        solveOnce(goal, SolveOptions.someLazilyWithTimeout(1, timeout))
    fun solveOnce(goal: String, options: SolveOptions): Solution =
        solve(goal, options.setLimit(1)).getSolution(0)

    fun getFlags(): FlagStore
    fun getStaticKB(): Theory
    fun getDynamicKB(): Theory
    fun getLibraries(): List<String>
    fun getUnificator(): Unificator
    fun getOperators(): OperatorSet
    fun getInputChannels(): Map<String, List<String>>
    fun getOutputChannels(): Map<String, List<String>>
    fun getId(): String
    fun writeOnInputChannel(channelID: String, vararg terms: String)
    fun readOnOutputChannel(channelID: String): String
    fun readStreamOnOutputChannel(channelID: String): BlockingDeque<String>
    fun closeClient(withDeletion: Boolean = false)

    companion object {
        @JvmStatic
        val prolog: ClientSolverFactory by lazy { ClientSolverFactory }
    }
}
