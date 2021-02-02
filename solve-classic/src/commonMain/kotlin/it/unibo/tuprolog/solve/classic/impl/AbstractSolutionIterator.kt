package it.unibo.tuprolog.solve.classic.impl

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.classic.SolutionIterator
import it.unibo.tuprolog.solve.classic.fsm.EndState
import it.unibo.tuprolog.solve.classic.fsm.State

internal abstract class AbstractSolutionIterator(state: State) : SolutionIterator {

    final override var state: State = state
        private set

    final override var step: Long = 0
        private set

    final override fun hasNext(): Boolean = state.let { it !is EndState || it.hasOpenAlternatives }

    final override fun next(): Solution {
        do {
            val previousState = state
            state = computeNextState(state, ++step)
            onStateTransition(previousState, state, step)
        } while (state !is EndState)
        return (state as EndState).solution.cleanUp()
    }

    protected abstract fun computeNextState(state: State, step: Long): State

    private fun Solution.cleanUp(): Solution =
        when (this) {
            is Solution.Yes -> cleanUp()
            else -> this
        }

    private fun Solution.Yes.cleanUp(): Solution.Yes {
        return copy(substitution = substitution.cleanUp(query.variables.toSet()))
    }

    private fun Substitution.Unifier.cleanUp(toRetain: Set<Var>): Substitution.Unifier {
        return filter { _, term -> (term !is Var) || (term in toRetain) }
    }
}
