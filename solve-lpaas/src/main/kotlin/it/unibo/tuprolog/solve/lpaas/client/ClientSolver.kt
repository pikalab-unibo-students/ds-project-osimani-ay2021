package it.unibo.tuprolog.solve.lpaas.client

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.prolog.SolutionsSequence
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import java.util.concurrent.BlockingDeque

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

    fun getFlags(): FlagStore
    fun getStaticKB(): Theory
    fun getDynamicKB(): Theory
    fun getLibraries(): List<String>
    fun getUnificator(): Unificator
    fun getOperators(): OperatorSet
    fun getInputChannels(): List<String>
    fun getOutputChannels(): List<String>
    fun writeOnInputChannel(channelID: String): StreamObserver<String>
    fun readOnOutputChannel(channelID: String): String
    fun readStreamOnOutputChannel(channelID: String): BlockingDeque<String>
    fun closeClient()

    companion object {
        @JvmStatic
        val prolog: PrologSolverFactory by lazy { PrologSolverFactory }
    }
}
