package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesParser
import kotlin.random.Random

object SolversCollection {

    private val theory = Theory.of(
        Fact.of( Struct.of("f", Atom.of("a"))),
        Fact.of( Struct.of("f", Atom.of("b"))),
        Fact.of( Struct.of("f", Atom.of("c")))
    )

    private val defaultSolver: Solver = Solver.prolog.solverWithDefaultBuiltins(staticKb = theory)

    private val solvers: MutableMap<String, Solver> = mutableMapOf()

    fun getSolverOrDefault(id: String): Solver {
        return solvers.getOrDefault(id, defaultSolver)
    }

    fun addSolver(solver: Solver): String {
        val id = idGenerator()
        solvers[id] = solver
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