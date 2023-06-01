package it.unibo.tuprolog.primitives.client.impl

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericGetMsg
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.client.ClientSession
import it.unibo.tuprolog.primitives.client.SessionSolver
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.primitive.Solve
import java.util.concurrent.LinkedBlockingDeque

class ClientSessionImpl(private val request: Solve.Request<ExecutionContext>, channel: ManagedChannel):
    ClientSession {

    private var closed = false

    private val scope = Scope.of(request.query)
    private val sessionSolver: SessionSolver
    private val responseStream: StreamObserver<SolverMsg>
    private val queue = LinkedBlockingDeque<Solve.Response>()

    init {
        responseStream = GenericPrimitiveServiceGrpc.newStub(channel).callPrimitive(this)
        responseStream.onNext(SolverMsg.newBuilder().setRequest(request.serialize()).build())
        sessionSolver = SessionSolver.of(responseStream, request.context)
    }

    override fun onNext(value: GeneratorMsg) {
        if(value.hasResponse()) {
            queue.add(value.response.deserialize(scope, request.context))
            if(!value.response.solution.hasNext) {
                closed = true
                this.onCompleted()
            }
        }
        else if(value.hasRequest()) {
            val request = value.request
            if(request.hasSubSolve())
                sessionSolver.solve(request.id, request.subSolve)
            else if(request.hasReadLine())
                sessionSolver.readLine(request.id, request.readLine)
            else if(request.hasInspectKb())
                sessionSolver.inspectKb(request.id, request.inspectKb)
            else if(request.hasGenericGet()) {
                val get = request.genericGet
                when(get.element) {
                    GenericGetMsg.Element.LOGIC_STACKTRACE ->
                        sessionSolver.getLogicStackTrace(request.id)
                    GenericGetMsg.Element.CUSTOM_DATA_STORE ->
                        sessionSolver.getCustomDataStore(request.id)
                    GenericGetMsg.Element.LIBRARIES ->
                        sessionSolver.getLibraries(request.id)
                    GenericGetMsg.Element.UNIFICATOR ->
                        sessionSolver.getUnificator(request.id)
                    GenericGetMsg.Element.FLAGS ->
                        sessionSolver.getFlagStore(request.id)
                    GenericGetMsg.Element.OPERATORS ->
                        sessionSolver.getOperators(request.id)
                    GenericGetMsg.Element.INPUT_CHANNELS ->
                        sessionSolver.getInputStoreAliases(request.id)
                    GenericGetMsg.Element.OUTPUT_CHANNELS ->
                        sessionSolver.getOutputStoreAliases(request.id)
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    override fun onError(t: Throwable?) {
        queue.add(
            request.replyException(ResolutionException(
                context = request.context,
                cause = t))
        )
        closed = true
    }

    override fun onCompleted() {
        closed = true
    }

    override val solutionsQueue: Iterator<Solve.Response> =
        object: Iterator<Solve.Response> {
            override fun hasNext(): Boolean = !closed

            override fun next(): Solve.Response {
                responseStream.onNext(
                    SolverMsg.newBuilder().setNext(EmptyMsg.getDefaultInstance()).build()
                )
                
                return queue.takeFirst()
            }
        }

}