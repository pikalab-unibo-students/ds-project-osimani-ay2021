package it.unibo.tuprolog.solve.lpaas.client.trasparent

import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.util.toMap
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

class TrasparentSolver(unificator: Unificator,
                                libraries: Runtime,
                                flags: FlagStore,
                                staticKb: Theory,
                                dynamicKb: Theory,
                                inputChannels: InputStore,
                                outputChannels: OutputStore
) : TrasparentClient() {

    override val solver: ClientSolver = ClientSolverFactory.solverOf(
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
            outputs = outputChannels.map {
                Pair(it.key) { msg: String -> it.value.write(msg)}
            }.toMap()
        )

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
    ): Solver {
        return TrasparentSolver(unificator, libraries, flags, staticKb, dynamicKb, InputStore.fromStandard(stdIn),
            OutputStore.fromStandard(stdOut, stdErr, warnings))
    }
}