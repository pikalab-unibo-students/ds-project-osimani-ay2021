package it.unibo.tuprolog.solve.lpaas.client.prolog

import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY_STRING
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse


object PrologSolverFactory  {
    fun basicClient(staticKb: String = "", dynamicKb: String = ""): ClientSolver {
        return ClientPrologSolverImpl(Theory.parse(staticKb), Theory.parse(dynamicKb))
    }
}
