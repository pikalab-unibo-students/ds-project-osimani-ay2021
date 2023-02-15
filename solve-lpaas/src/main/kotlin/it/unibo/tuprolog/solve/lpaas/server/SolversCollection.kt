package it.unibo.tuprolog.solve.lpaas.server

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesParser

object SolversCollection {

    private val theory = """
                f(b).
                f(d).
                """.trimIndent()

    val parser = ClausesParser.withDefaultOperators()

    /**Default no more needed?**/
    private val defaultSolver: Solver = Solver.prolog.solverWithDefaultBuiltins(staticKb = parser.parseTheory(theory))

    private val solvers: MutableMap<String, Solver> = mutableMapOf()

    fun getSolver(id: String): Solver {
        return solvers.getOrDefault(id, defaultSolver)
    }

    fun addSolver(sKb: String = theory, dKb: String = ""): String {
        var staticKb: Theory
        var dynamicKb: Theory
        try {
            staticKb = parser.parseTheory(sKb)
            dynamicKb = parser.parseTheory(dKb)
        } catch (e: Exception) {
            staticKb = Theory.empty()
            dynamicKb = Theory.empty()
        }
        val id = idGenerator()
        solvers[id] = Solver.prolog.solverWithDefaultBuiltins(
            staticKb = staticKb,
            dynamicKb = dynamicKb
        )
        return id
    }

    private const val STRING_LENGTH = 10
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun idGenerator(): String {
        val id = List(STRING_LENGTH) { charPool.random() }.joinToString("")
        return if (solvers.containsKey(id)) idGenerator()
            else id
    }
}