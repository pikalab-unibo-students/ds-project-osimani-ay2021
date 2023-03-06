package it.unibo.tuprolog.solve.lpaas.server.utils

import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.solve.lpaas.util.idGenerator
import it.unibo.tuprolog.unify.Unificator

import it.unibo.tuprolog.solve.lpaas.util.toMap

object SolversCollection {

    private const val SOLVER_CODE = "SV"

    private val solvers: MutableMap<String, Solver> = mutableMapOf()

    private val solversDeques: MutableMap<String, ChannelsDequesCollector> = mutableMapOf()


    /** Include error instead of default? **/
    fun getSolver(id: String): Solver {
        return solvers[id]!!
    }

    fun getChannelDequesOfSolver(id: String): ChannelsDequesCollector {
        return solversDeques[id]!!
    }

    fun addSolver(unificator: Unificator, runtime: Runtime, flagStore: FlagStore,
                  staticKb: Theory, dynamicKb: Theory,
                  /*/operatorSet: OperatorSet,*/ inputs: Map<String, String>,
                  outputs: Set<String>): String {
        var id: String
        do {id = idGenerator()+ SOLVER_CODE
        } while (solvers.containsKey(id))

        val channelsDeque = ChannelsDequesCollector(outputs = outputs)
        solversDeques[id] = channelsDeque

        inputs.forEach {channelsDeque.writeOnInputChannel(it.key, it.value) }

        val inputChannels = channelsDeque.getAllInputs().map {
            Pair(it.key, InputChannel.of { -> it.value.takeFirst() })}.toMap()
        val outputChannels = channelsDeque.getAllOutputs().map {
            Pair(it.key, OutputChannel.of { line: String-> it.value.putLast(line) })}.toMap()

        solvers[id] = Solver.prolog.solverOf(
            unificator = unificator,
            libraries = runtime,
            flags = flagStore,
            staticKb = staticKb,
            dynamicKb = dynamicKb,
            inputs = InputStore.of(inputChannels),
            outputs = OutputStore.of(outputChannels)
        )
        return id
    }
}