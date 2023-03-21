package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory

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
    fun setStandardInput(content: String)
    fun setStandardOutput(stdOut: OutputChannel<String>)
    fun setStandardError(stdErr: OutputChannel<String>)
    fun setWarnings(stdWarn: OutputChannel<Warning>)
}
