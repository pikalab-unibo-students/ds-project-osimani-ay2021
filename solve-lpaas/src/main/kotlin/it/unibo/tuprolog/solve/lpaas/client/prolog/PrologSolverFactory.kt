package it.unibo.tuprolog.solve.lpaas.client.prolog

import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse


object PrologSolverFactory  {
    fun solverOf(unificator: Map<String, String> = emptyMap(), libraries: Set<String> = emptySet(),
                 flags: Map<String, String> = emptyMap(), staticKb: Theory = Theory.empty(),
                 dynamicKb: Theory = Theory.empty(), operators: Map<String, Pair<String, Int>> = emptyMap(),
                 inputChannels: Map<String, String> = emptyMap(), outputs: Set<String> = emptySet(),
                 defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(unificator, libraries, flags, staticKb, dynamicKb,
            operators, inputChannels, outputs, defaultBuiltins)
    }

    fun solverOf(unificator: Map<String, String> = emptyMap(), libraries: Set<String> = emptySet(),
                 flags: Map<String, String> = emptyMap(), staticKb: String = "",
                 dynamicKb: String = "", operators: Map<String, Pair<String, Int>> = emptyMap(),
                 inputChannels: Map<String, String> = emptyMap(), outputs: Set<String> = emptySet(),
                 defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(unificator, libraries, flags, Theory.parse(staticKb), Theory.parse(dynamicKb),
            operators, inputChannels, outputs, defaultBuiltins)
    }

    fun mutableSolverOf(unificator: Map<String, String> = emptyMap(), libraries: Set<String> = emptySet(),
                        flags: Map<String, String> = emptyMap(), staticKb: Theory = Theory.empty(),
                        dynamicKb: Theory = Theory.empty(), operators: Map<String, Pair<String, Int>> = emptyMap(),
                        inputChannels: Map<String, String> = emptyMap(), outputs: Set<String> = emptySet(),
                        defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologMutableSolverImpl(unificator, libraries, flags, staticKb, dynamicKb,
            operators, inputChannels, outputs, defaultBuiltins)
    }
}
