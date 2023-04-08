package it.unibo.tuprolog.solve.lpaas.client.trasparent

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.flags.NotableFlag
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.util.toMap
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

class TrasparentMutableSolver(unificator: Unificator,
                              libraries: Runtime,
                              flags: FlagStore,
                              staticKb: Theory,
                              dynamicKb: Theory,
                              inputChannels: InputStore,
                              outputChannels: OutputStore
) : TrasparentClient(), MutableSolver {

    override val solver: ClientMutableSolver = ClientPrologSolverFactory.mutableSolverOf(
            unificator = unificator,
            libraries =  libraries.aliases.toSet(),
            flags = flags,
            staticKb = staticKb,
            dynamicKb = dynamicKb,
            inputs = inputChannels.map {
                var content = ""
                val channel = it.value
                while(channel.peek() != null) {
                    content = content.plus(channel.read())
                }
                Pair(it.key, content)
            }.toMap(),
            outputs = outputChannels.currentAliases.associateBy { "" }
        )

    override fun loadLibrary(library: Library) = solver.loadLibrary(library.alias)
    override fun unloadLibrary(library: Library) = solver.unloadLibrary(library.alias)
    override fun setRuntime(libraries: Runtime) = solver.setRuntime(libraries.aliases)
    override fun loadStaticKb(theory: Theory) = solver.loadStaticKb(theory)
    override fun appendStaticKb(theory: Theory) = solver.appendStaticKb(theory)
    override fun resetStaticKb() = solver.resetStaticKb()
    override fun loadDynamicKb(theory: Theory) = solver.loadDynamicKb(theory)
    override fun appendDynamicKb(theory: Theory) = solver.appendDynamicKb(theory)
    override fun resetDynamicKb() = solver.resetDynamicKb()
    override fun assertA(clause: Clause) = solver.assertA(clause)
    override fun assertA(fact: Struct) = solver.assertA(fact)
    override fun assertZ(clause: Clause) = solver.assertZ(clause)
    override fun assertZ(fact: Struct) = solver.assertZ(fact)
    override fun retract(clause: Clause): RetractResult<Theory> = solver.retract(clause)
    override fun retract(fact: Struct): RetractResult<Theory> = solver.retract(fact)
    override fun retractAll(clause: Clause): RetractResult<Theory> = solver.retractAll(clause)
    override fun retractAll(fact: Struct): RetractResult<Theory> = solver.retractAll(fact)
    override fun setFlag(name: String, value: Term) = solver.setFlag(name, value)
    override fun setFlag(flag: Pair<String, Term>) = solver.setFlag(flag.first, flag.second)
    override fun setFlag(flag: NotableFlag) = solver.setFlag(flag.name, flag.defaultValue)
    override fun setStandardInput(stdIn: InputChannel<String>) {
        var content = ""
        while(stdIn.peek() != null) {
            content = content.plus(stdIn.read())
        }
        solver.setStandardInput(content)
    }
    override fun setStandardError(stdErr: OutputChannel<String>) {
        solver.setStandardError(stdErr)
    }
    override fun setStandardOutput(stdOut: OutputChannel<String>) {
        solver.setStandardOutput(stdOut)
    }
    override fun setWarnings(warnings: OutputChannel<Warning>) {
        solver.setWarnings(warnings)
    }
    override fun copy(
        unificator: Unificator,
        libraries: Runtime,
        flags: FlagStore,
        staticKb: Theory,
        dynamicKb: Theory,
        stdIn: InputChannel<String>,
        stdOut: OutputChannel<String>,
        stdErr: OutputChannel<String>,
        warnings: OutputChannel<Warning>
    ): MutableSolver {
        return TrasparentMutableSolver(unificator, libraries, flags, staticKb, dynamicKb,
            InputStore.fromStandard(stdIn), OutputStore.fromStandard(
                stdOut, stdErr, warnings))
    }
    override fun clone(): MutableSolver = copy()
}