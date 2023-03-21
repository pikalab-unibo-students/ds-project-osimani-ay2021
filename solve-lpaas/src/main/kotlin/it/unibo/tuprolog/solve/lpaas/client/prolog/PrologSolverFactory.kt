package it.unibo.tuprolog.solve.lpaas.client.prolog

import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.SolverFactory
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.classic.stdlib.DefaultBuiltins
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import it.unibo.tuprolog.unify.Unificator


object PrologSolverFactory  {
    fun solverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                 flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                 dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                 outputs: Set<String> = emptySet(), defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(unificator, libraries, flags, staticKb, dynamicKb,
            inputs, outputs, defaultBuiltins)
    }

    fun solverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                 flags: FlagStore = FlagStore.empty(), staticKb: String = "",
                 dynamicKb: String = "", inputs: Map<String, String> = emptyMap(),
                 outputs: Set<String> = emptySet(), defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(unificator, libraries, flags, Theory.parse(staticKb),
            Theory.parse(dynamicKb), inputs, outputs, defaultBuiltins)
    }

    fun mutableSolverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                        flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                        dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                        outputs: Set<String> = emptySet(), defaultBuiltins: Boolean = true): ClientMutableSolver {
        return ClientPrologMutableSolverImpl(unificator, libraries, flags, staticKb, dynamicKb,
            inputs, outputs, defaultBuiltins)
    }
}
