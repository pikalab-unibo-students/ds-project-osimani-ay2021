package it.unibo.tuprolog.primitives.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitiveWrapper
import it.unibo.tuprolog.primitives.server.session.impl.ServerSessionImpl
import it.unibo.tuprolog.solve.Signature
import java.util.concurrent.Executor

class PrimitiveServerWrapper private constructor(
    functor: String,
    arity: Int,
    private val primitive: DistribuitedPrimitive,
    private val executor: Executor
): GenericPrimitiveServiceGrpc.GenericPrimitiveServiceImplBase()  {

    val signature: Signature by lazy { Signature(functor, arity) }

    override fun callPrimitive(responseObserver: StreamObserver<GeneratorMsg>): StreamObserver<SolverMsg> {
        return ServerSessionImpl(primitive, responseObserver, executor)
    }

    /** Respond with the signature of the primitive **/
    override fun getSignature(request: EmptyMsg?, responseObserver: StreamObserver<SignatureMsg>) {
        responseObserver.onNext(signature.serialize())
        responseObserver.onCompleted()
    }

    companion object {
        fun of(
            functor: String,
            arity: Int,
            primitive: DistribuitedPrimitive,
            executor: Executor
        ): PrimitiveServerWrapper =
            PrimitiveServerWrapper(functor, arity, primitive, executor)

        fun of(
            primitive: DistribuitedPrimitiveWrapper,
            executor: Executor
        ): PrimitiveServerWrapper =
            of(primitive.signature.name, primitive.signature.arity, primitive.implementation, executor)
    }
}

