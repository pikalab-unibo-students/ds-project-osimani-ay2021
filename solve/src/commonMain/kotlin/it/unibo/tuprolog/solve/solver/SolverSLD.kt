package it.unibo.tuprolog.solve.solver

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution.Companion.asUnifier
import it.unibo.tuprolog.primitive.extractSignature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solve
import it.unibo.tuprolog.solve.solver.statemachine.StateMachineExecutor
import it.unibo.tuprolog.solve.solver.statemachine.state.FinalState
import it.unibo.tuprolog.solve.solver.statemachine.state.StateEnd
import it.unibo.tuprolog.solve.solver.statemachine.state.StateInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Default implementation of SLD (*Selective Linear Definite*) solver, exploring the search tree
 *
 * @author Enrico
 */
internal class SolverSLD(
        override val startContext: ExecutionContextImpl = ExecutionContextImpl(),
        private val executionStrategy: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AbstractSolver(startContext) {

    override fun solve(goal: Struct): Sequence<Solution> =
            solve(Solve.Request(goal.extractSignature(), goal.argsList, goal, startContext))
                    .map { it.solution.withOnlyAnswerSubstitution() }

    /** Internal version of other [solve] method, that accepts raw requests and returns raw responses */
    internal fun solve(goalRequest: Solve.Request<ExecutionContextImpl>): Sequence<Solve.Response> = StateMachineExecutor
            .execute(StateInit(goalRequest, executionStrategy))
            .filterIsInstance<FinalState>()
            .filter { it.solveRequest.equalSignatureAndArgs(goalRequest) }
            .map { Solve.Response(it.toSolution(), context = it.solveRequest.context) }

    /** Utility method to map a [FinalState] to its corresponding [Solution] */
    private fun FinalState.toSolution() = when (this) {
        is StateEnd.True -> with(solveRequest) { Solution.Yes(signature, arguments, context.substitution) }
        is StateEnd.Halt -> with(solveRequest) { Solution.Halt(signature, arguments, exception) }
        else -> with(solveRequest) { Solution.No(signature, arguments) }
    }

    // this should become useless when substitutions will be cleaned, while performing resolution
    /** Utility function to calculate answerSubstitution on Solution.Yes */
    private fun Solution.withOnlyAnswerSubstitution() = when (this) {
        // TODO: 25/09/2019 extract this out from there and insert in SolverUtils to be tested and used in tests
        is Solution.Yes ->
            // reduce substitution variable chains
            copy(substitution = with(substitution) { this.mapValues { (_, term) -> term.apply(this) } }
                    .filterKeys { it in query.variables }.asUnifier())
        else -> this
    }
}
