package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.flags.NotableFlag
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.prolog.SolutionsSequence
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.theory.RetractResult
import java.util.concurrent.BlockingDeque

interface ClientMutableSolver: ClientSolver  {
    fun loadLibrary(libraryName: String)

    fun unloadLibrary(libraryName: String)

    fun setRuntime(libraries: Set<String>)

    fun loadStaticKb(theory: Theory)

    fun appendStaticKb(theory: Theory)

    fun resetStaticKb()

    fun loadDynamicKb(theory: Theory)

    fun appendDynamicKb(theory: Theory)

    fun resetDynamicKb()

    fun assertA(fact: Struct)

    fun assertZ(fact: Struct)

    fun retract(fact: Struct): RetractResult<Theory>

    fun retractAll(fact: Struct): RetractResult<Theory>

    fun setFlag(name: String, value: Term)

    fun setStandardInput(name: String, content: String)

    fun setStandardError(name: String)

    fun setStandardOutput(name: String)

    fun setWarnings(name: String)
}
