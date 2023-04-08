package testGui

import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.solve.ExecutionContextAware
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.joinAll
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

data class SolverEvent<T>(
    val event: T,
    override val unificator: Unificator,
    override val operators: OperatorSet,
    override val libraries: Runtime,
    override val flags: FlagStore,
    override val staticKb: Theory,
    override val dynamicKb: Theory,
    override val inputChannels: InputStore,
    override val outputChannels: OutputStore,
    val solverId: String
) : ExecutionContextAware {
    constructor(event: T, other: ClientMutableSolver) :
        this(
            event = event,
            unificator = other.getUnificator(),
            dynamicKb = other.getDynamicKB().toImmutableTheory(),
            flags = other.getFlags(),
            inputChannels = InputStore.of(other.getInputChannels()
                .map { Pair(it.key, InputChannel.of(it.value.joinAll())) }.toMap()),
            libraries = Runtime.of(other.getLibraries().map { convertStringToKnownLibrary(it) }),
            operators = other.getOperators(),
            outputChannels = OutputStore.of(other.getOutputChannels().keys.associateWith { OutputChannel.of {  } }),
            staticKb = other.getStaticKB().toImmutableTheory(),
            solverId = other.getId()
        )
}
