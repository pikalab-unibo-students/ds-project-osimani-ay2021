package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.prolog.SolutionsSequence

interface SimpleSolver  {
    fun solve(goal: String): SolutionsSequence

    fun closeClient()

    companion object {
        @JvmStatic
        val prolog: PrologSolverFactory by lazy { PrologSolverFactory }
    }
}
