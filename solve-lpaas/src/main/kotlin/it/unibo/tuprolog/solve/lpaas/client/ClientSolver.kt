package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.prolog.SolutionsSequence

interface ClientSolver  {
    fun solve(goal: String): SolutionsSequence
    fun solve(goal: String, timeout: TimeDuration): SolutionsSequence
    fun solve(goal: String, options: SolveOptions): SolutionsSequence
    fun solveList(goal: String, timeout: TimeDuration): List<Solution>
    fun solveList(goal: String): List<Solution>
    fun solveList(goal: String, options: SolveOptions): List<Solution>
    fun solveOnce(goal: String, timeout: TimeDuration): Solution
    fun solveOnce(goal: String): Solution
    fun solveOnce(goal: String, options: SolveOptions): Solution

    fun closeClient()

    companion object {
        @JvmStatic
        val prolog: PrologSolverFactory by lazy { PrologSolverFactory }
    }
}
