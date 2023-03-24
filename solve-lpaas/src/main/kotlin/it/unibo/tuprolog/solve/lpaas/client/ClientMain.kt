package it.unibo.tuprolog.solve.lpaas.client

import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY

fun main() {
    println("Welcome to Client for Logic Programming")
    val input = readln()
    if(input.startsWith("solve")) {
        val solver = PrologSolverFactory.solverOf(staticKb = DEFAULT_STATIC_THEORY)
        println("Connected to ${solver.getId()}")
        val solutions = solver.solve(input.removePrefix("solve ")).asSequence().toList()
        solutions.forEach {
            println(it.solvedQuery)
        }
    } else if(input.startsWith("connect")) {
        val solver = PrologSolverFactory.connectToSolver(input.removePrefix("connect "))
        println("Connected to ${solver!!.getId()}")
        while(true) {
            val request = readln()
            if (request.startsWith("solve")) {
                val solutions = solver.solve(request.removePrefix("solve ")).asSequence().toList()
                solutions.forEach {
                    println(it.solvedQuery)
                }
            } else if(request.startsWith("quit")) return
        }
    }
}