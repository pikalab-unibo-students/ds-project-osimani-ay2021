package testGui

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.exception.TuPrologException
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.parsing.ParseException
import it.unibo.tuprolog.core.parsing.parseAsStruct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.libs.io.IOLib
import it.unibo.tuprolog.solve.libs.oop.OOPLib
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverFactory
import testGui.TuPrologIDEModel.State
import it.unibo.tuprolog.theory.Theory
import org.reactfx.EventSource
import java.util.*
import java.util.concurrent.ExecutorService

internal class TuPrologIDEModelImpl(
    override val executor: ExecutorService,
    var customizer: ((ClientMutableSolver) -> ClientMutableSolver)? = { it }
) : TuPrologIDEModel {

    private var solutions: Iterator<Solution>? = null

    private var solutionCount = 0

    private var lastGoal: Struct? = null

    override var query: String = ""

    private var currentSolver: ClientMutableSolver? = null

    private var stdin = ""

    override var solveOptions: SolveOptions = SolveOptions.DEFAULT.setTimeout(5000)
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                onSolveOptionsChanged.push(value)
            }
        }

    override fun newSolver(theory: Theory): String {
        val solver = ClientPrologSolverFactory.mutableSolverOf(
        staticKb = theory,
        libraries = setOf(OOPLib, IOLib).map { it.alias }.toSet())
        solver.setStandardInput(stdin)
        solver.setStandardOutput( OutputChannel.of { onStdoutPrinted.push(it) } )
        solver.setStandardError( OutputChannel.of { onStderrPrinted.push(it) } )
        solver.setWarnings( OutputChannel.of { onWarning.push(it) } )
        return solver.getId().also { loadSolver(it) }
    }

    override fun loadSolver(solverId: String) {
        currentSolver = ClientPrologSolverFactory.connectToMutableSolver(solverId)
        if(customizer != null) customizer?.invoke(currentSolver!!)
        onSolverLoaded.push(SolverEvent(Unit, currentSolver!!))
    }

    override fun getCurrentSolver(): ClientMutableSolver? = currentSolver

    override fun customizeSolver(customizer: (ClientMutableSolver) -> ClientMutableSolver) {
        this.customizer = customizer
    }

    override fun setStdin(content: String) {
        ensuringStateIs(State.IDLE) {
            stdin = content
            if(currentSolver != null) currentSolver!!.setStandardInput(content)
        }
    }

    override fun quit() {
        this.closeSolver()
        onQuit.push(Unit)
    }

    @Volatile
    override var state: State = State.IDLE
        private set

    private inline fun <T> ensuringStateIs(state: State, vararg states: State, action: () -> T): T {
        if (EnumSet.of(state, *states).contains(this.state)) {
            return action()
        } else {
            throw IllegalStateException()
        }
    }

    override fun closeSolver() {
        onSolverClosed.push(currentSolver!!.getId())
        currentSolver?.closeClient()
    }

    override fun reset() {
        ensuringStateIs(State.IDLE, State.SOLUTION) {
            if (state == State.SOLUTION) {
                stop()
            }
            try {
                currentSolver!!.resetDynamicKb()
                onReset.push(SolverEvent(Unit, currentSolver!!))
            } catch (e: SyntaxException) {
                onError.push(e)
            }
        }
    }

    override fun solve() {
        solveImpl {
            state = State.COMPUTING
            nextImpl()
        }
    }

    override fun solveAll() {
        solveImpl {
            state = State.COMPUTING
            onResolutionStarted.push(SolverEvent(++solutionCount, currentSolver!!))
            nextAllImpl()
        }
    }

    private fun solveImpl(continuation: () -> Unit) {
        ensuringStateIs(State.IDLE, State.SOLUTION) {
            try {
                solutions = newResolution()
                solutionCount = 0
                onNewQuery.push(SolverEvent(lastGoal!!, currentSolver!!))
                continuation()
            } catch (e: SyntaxException) {
                onError.push(e)
            }
        }
    }

    private fun newResolution(): Iterator<Solution> {
        currentSolver!!.let {
            lastGoal = parseQueryAsStruct(it.getOperators())
            return it.solve(lastGoal!!, solveOptions).iterator()
        }
    }

    private fun parseQueryAsStruct(operators: OperatorSet): Struct {
        try {
            return query.parseAsStruct(operators)
        } catch (e: ParseException) {
            throw SyntaxException.InQuerySyntaxError(query, e)
        }
    }

    private fun nextImpl() {
        executor.execute {
            onResolutionStarted.push(SolverEvent(++solutionCount, currentSolver!!))
            val sol = solutions!!.next()
            onResolutionOver.push(SolverEvent(solutionCount, currentSolver!!))
            onNewSolution.push(SolverEvent(sol, currentSolver!!))
            state = if (!sol.isYes || !solutions!!.hasNext()) {
                onQueryOver.push(SolverEvent(sol.query, currentSolver!!))
                State.IDLE
            } else {
                State.SOLUTION
            }
        }
    }

    private fun nextAllImpl() {
        executor.submit {
            val sol = solutions!!.next()
//            onResolutionOver.push(solutionCount)
            solutionCount++
            onNewSolution.push(SolverEvent(sol, currentSolver!!))
            if (!solutions!!.hasNext() || state != State.COMPUTING) {
                onResolutionOver.push(SolverEvent(solutionCount, currentSolver!!))
                onQueryOver.push(SolverEvent(sol.query, currentSolver!!))
                state = State.IDLE
            } else {
                nextAllImpl()
            }
        }
    }

    override fun next() {
        ensuringStateIs(State.SOLUTION) {
            state = State.COMPUTING
            nextImpl()
        }
    }

    override fun nextAll() {
        ensuringStateIs(State.SOLUTION) {
            state = State.COMPUTING
            onResolutionStarted.push(SolverEvent(solutionCount, currentSolver!!))
            nextAllImpl()
        }
    }

    override fun stop() {
        ensuringStateIs(State.SOLUTION) {
            state = State.IDLE
            onQueryOver.push(SolverEvent(lastGoal!!, currentSolver!!))
        }
    }

    override val onQuit: EventSource<Unit> = EventSource()

    override val onReset: EventSource<SolverEvent<Unit>> = EventSource()

    override val onSolveOptionsChanged: EventSource<SolveOptions> = EventSource()

    override val onSolverLoaded: EventSource<SolverEvent<Unit>> = EventSource()

    override val onSolverClosed: EventSource<String> = EventSource()

    override val onQueryChanged: EventSource<String> = EventSource()

    override val onNewQuery: EventSource<SolverEvent<Struct>> = EventSource()

    override val onNewSolver: EventSource<SolverEvent<Unit>> = EventSource()

    override val onResolutionStarted: EventSource<SolverEvent<Int>> = EventSource()

    override val onNewSolution: EventSource<SolverEvent<Solution>> = EventSource()

    override val onResolutionOver: EventSource<SolverEvent<Int>> = EventSource()

    override val onQueryOver: EventSource<SolverEvent<Struct>> = EventSource()

    override val onStdoutPrinted: EventSource<String> = EventSource()

    override val onStderrPrinted: EventSource<String> = EventSource()

    override val onWarning: EventSource<Warning> = EventSource()

    override val onError: EventSource<TuPrologException> = EventSource()
}
