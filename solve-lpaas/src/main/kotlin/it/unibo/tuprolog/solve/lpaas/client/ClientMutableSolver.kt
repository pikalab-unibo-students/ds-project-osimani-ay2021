package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
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
    fun setStandardInput(name: String, content: String)
    fun setStandardError(name: String)
    fun setStandardOutput(name: String)
    fun setWarnings(name: String)
}
