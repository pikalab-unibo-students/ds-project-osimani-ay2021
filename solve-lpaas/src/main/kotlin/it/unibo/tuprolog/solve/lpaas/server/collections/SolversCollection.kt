package it.unibo.tuprolog.solve.lpaas.server.collections

import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.classic.stdlib.DefaultBuiltins
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.server.channels.ChannelsDequesCollector
import it.unibo.tuprolog.solve.lpaas.server.database.DbManager
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.solve.lpaas.util.idGenerator
import it.unibo.tuprolog.unify.Unificator

object SolversCollection {

    private const val SOLVER_CODE = "SV"

    private val solvers: MutableMap<String, Solver> = mutableMapOf()

    private val solversDeques: MutableMap<String, ChannelsDequesCollector> = mutableMapOf()

    init {
        DbManager.loadSolvers()
    }

    /** Include error instead of default? **/
    fun getSolver(id: String): Solver {
        return solvers[id]!!
    }

    fun getMutableSolver(id: String): MutableSolver? {
        return if (solvers[id] is MutableSolver) solvers[id] as MutableSolver else null
    }

    fun getChannelDequesOfSolver(id: String): ChannelsDequesCollector {
        return solversDeques[id]!!
    }

    fun contains(solverId: String): Boolean = solvers.containsKey(solverId)

    fun addSolver(unificator: Unificator, runtime: Runtime, flagStore: FlagStore,
                  staticKb: Theory, dynamicKb: Theory, inputs: Map<String, List<String>>,
                  outputs: Map<String, List<String>>, mutable: Boolean, defaultBuiltIns: Boolean,
                  id: String = generateId()): String {
        val channelsDeque = ChannelsDequesCollector.of(inputs, outputs)
        solversDeques[id] = channelsDeque

        val libraries = if(defaultBuiltIns && !runtime.containsKey(DefaultBuiltins.alias))
            runtime + Solver.prolog.defaultBuiltins
        else runtime

        solvers[id] = if(mutable) {
            Solver.prolog.mutableSolverOf(
                unificator = unificator,
                libraries = libraries,
                flags = flagStore,
                staticKb = staticKb,
                dynamicKb = dynamicKb,
                inputs = InputStore.of(channelsDeque.getInputChannels()),
                outputs = OutputStore.of(channelsDeque.getOutputChannels(), channelsDeque.getWarningChannel()))
        } else {
            Solver.prolog.solverOf(
            unificator = unificator,
            libraries = libraries,
            flags = flagStore,
            staticKb = staticKb,
            dynamicKb = dynamicKb,
            inputs = InputStore.of(channelsDeque.getInputChannels()),
            outputs = OutputStore.of(channelsDeque.getOutputChannels(), channelsDeque.getWarningChannel()))
        }

        return id
    }

    private fun generateId(): String {
        var id: String
        do {id = idGenerator()+ SOLVER_CODE
        } while (solvers.containsKey(id))
        return id
    }
}