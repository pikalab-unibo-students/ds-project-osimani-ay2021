package it.unibo.tuprolog.solve.lpaas.client.prolog

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY

/**
 * General type for logic solvers, i.e. any entity capable of solving some logic query -- provided as a [Struct] --
 * according to some logic, implementing one or more inference rule, via some resolution strategy.
 *
 * __Solvers are not immutable entities__. Their state may mutate as an effect of solving queries.
 */
object PrologSolverFactory  {
    fun basicClient(staticKb: String = DEFAULT_STATIC_THEORY, dynamicKb: String = ""): SimpleSolver {
        return ClientPrologSolverImpl(staticKb, dynamicKb)
    }
}
