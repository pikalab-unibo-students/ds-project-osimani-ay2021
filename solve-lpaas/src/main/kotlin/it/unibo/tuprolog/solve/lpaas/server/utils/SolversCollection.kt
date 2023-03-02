package it.unibo.tuprolog.solve.lpaas.server.utils

import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesParser
import it.unibo.tuprolog.solve.lpaas.util.idGenerator

object SolversCollection {

    private const val SOLVER_CODE = "SV"

    private val parser = ClausesParser.withDefaultOperators()

    private val solvers: MutableMap<String, Solver> = mutableMapOf()

    private val defaultSolver: Solver = Solver.prolog.solverWithDefaultBuiltins(
        staticKb = parser.parseTheory(DEFAULT_STATIC_THEORY)
    )

    /** Include error instead of default? **/
    fun getSolver(id: String): Solver {
        return solvers.getOrDefault(id, defaultSolver)
    }

    fun addSolver(sKb: String = DEFAULT_STATIC_THEORY, dKb: String = ""): String {
        var staticKb: Theory
        var dynamicKb: Theory
        try {
            staticKb = parser.parseTheory(sKb)
            dynamicKb = parser.parseTheory(dKb)
        } catch (e: Exception) {
            staticKb = Theory.empty()
            dynamicKb = Theory.empty()
        }
        var id: String
        do {id = idGenerator()+ SOLVER_CODE
        } while (solvers.containsKey(id))
        solvers[id] = Solver.prolog.solverWithDefaultBuiltins(
            staticKb = staticKb,
            dynamicKb = dynamicKb
        )
        return id
    }
}