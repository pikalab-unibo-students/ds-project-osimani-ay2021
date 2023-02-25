package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory

interface SimpleSolver  {
    fun solve(goal: String, timeout: TimeDuration = SolveOptions.MAX_TIMEOUT): Sequence<Solution>
    fun solve(goal: String): Sequence<Solution>
    fun solve(goal: String, options: SolveOptions): Sequence<Solution>
    fun solveList(goal: String, timeout: TimeDuration = SolveOptions.MAX_TIMEOUT): List<Solution>
    fun solveList(goal: String): List<Solution>
    fun solveList(goal: String, options: SolveOptions): List<Solution>

    fun solveOnce(goal: String, timeout: TimeDuration = SolveOptions.MAX_TIMEOUT): Sequence<Solution>

    fun solveOnce(goal: String): Sequence<Solution>

    fun solveOnce(goal: String, options: SolveOptions): Sequence<Solution>

    companion object {
        //@JvmStatic
        val prolog: PrologSolverFactory by lazy { PrologSolverFactory }
    }
}
