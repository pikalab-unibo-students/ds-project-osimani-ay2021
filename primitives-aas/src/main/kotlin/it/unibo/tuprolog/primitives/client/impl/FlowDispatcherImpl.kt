package it.unibo.tuprolog.primitives.client.impl

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.client.FlowDispatcher
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class FlowDispatcherImpl(request: Solve.Request<ExecutionContext>): FlowDispatcher {

    private val sessionSolver: SessionSolver = SessionSolver.of(request.context)
    private var closed: Boolean = true
    private val scope: Scope = Scope.of(request.query)
    private val msgQueue: Channel<SolverMsg> = Channel()
    private val solutionQueue: Channel<Solve.Response> = Channel()

    init {
        runBlocking {
            msgQueue.send(SolverMsg.newBuilder().setRequest(request.serialize()).build())
        }
    }

    override suspend fun handleMessage(msg: GeneratorMsg) {
        if(msg.hasResponse()) {
            solutionQueue.send(msg.response.deserialize(scope))
            if(!msg.response.solution.hasNext) {
                closed = true
            }
        }
        else if(msg.hasReadLine()) {
            msgQueue.send(sessionSolver.readLine(msg.readLine))
        }
        else if(msg.hasSubSolve()) {
            msgQueue.send(sessionSolver.solve(msg.subSolve))
        }
    }

    override suspend fun getMessage(): SolverMsg {
        return msgQueue.receive()
    }

    override suspend fun popResponse(): Solve.Response = solutionQueue.receive()

    override val isClosed: Boolean
        get() = closed
}