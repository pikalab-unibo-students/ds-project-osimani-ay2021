package it.unibo.tuprolog.primitives.server

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitiveWrapper
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.solve.Signature
import java.util.concurrent.Executor

class PrimitiveServerWrapper private constructor(
    functor: String,
    arity: Int,
    private val primitive: DistributedPrimitive,
    private val executor: Executor
): GenericPrimitiveServiceGrpc.GenericPrimitiveServiceImplBase()  {

    val signature: Signature by lazy { Signature(functor, arity) }

    override fun callPrimitive(responseObserver: StreamObserver<GeneratorMsg>): StreamObserver<SolverMsg> {
        return object: StreamObserver<SolverMsg> {

            private var session: ServerSession? = null

            override fun onNext(value: SolverMsg) {
                when(session) {
                    null ->
                        if (value.hasRequest())
                            session = ServerSession.of(primitive, value.request, responseObserver)
                        else
                            responseObserver.onError(
                                IllegalArgumentException("The request has not been initialized")
                            )
                    else -> {
                        executor.execute {
                            session!!.handleMessage(value)
                        }
                    }
                }
            }

            override fun onError(t: Throwable?) {
                if (t!! is StatusRuntimeException &&
                    (t as StatusRuntimeException).status.code == Status.CANCELLED.code)
                    println("Connection ended by client")
                else {
                    t.let {
                        throw t
                    }
                }
            }

            override fun onCompleted() {}
        }
    }

    /** Respond with the signature of the primitive **/
    override fun getSignature(request: EmptyMsg?, responseObserver: StreamObserver<SignatureMsg>) {
        responseObserver.onNext(signature.serialize())
        responseObserver.onCompleted()
    }

    companion object {
        fun of(
            name: String,
            arity: Int,
            primitive: DistributedPrimitive,
            executor: Executor
        ): PrimitiveServerWrapper =
            PrimitiveServerWrapper(name, arity, primitive, executor)

        fun of(
            primitive: DistributedPrimitiveWrapper,
            executor: Executor
        ): PrimitiveServerWrapper =
            of(primitive.signature.name, primitive.signature.arity, primitive.implementation, executor)
    }
}

