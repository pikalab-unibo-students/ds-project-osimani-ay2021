package it.unibo.tuprolog.primitives.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.session.SessionObserver
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper

class PrimitiveServerWrapper private constructor(val signature: Signature,
                                                 private val primitive: PrimitiveWithSession
):
    GenericPrimitiveServiceGrpc.GenericPrimitiveServiceImplBase()  {

    override fun callPrimitive(responseObserver: StreamObserver<GeneratorMsg>): StreamObserver<SolverMsg> {
        return SessionObserver(responseObserver, primitive)
    }

    /** Respond with the signature of the primitive **/
    override fun getSignature(request: EmptyMsg?, responseObserver: StreamObserver<SignatureMsg>) {
        responseObserver.onNext(signature.serialize())
        responseObserver.onCompleted()
    }

    companion object {

        fun of(functor: String, arity: Int,
               primitive: PrimitiveWithSession
        ): PrimitiveServerWrapper =
            PrimitiveServerWrapper(Signature(functor, arity), primitive)

        fun of(signature: Signature,
               primitive: PrimitiveWithSession
        ): PrimitiveServerWrapper =
            PrimitiveServerWrapper(signature, primitive)

        fun from(primitive: PrimitiveWrapper<ExecutionContext>) =
            PrimitiveServerWrapper(primitive.signature) {request, _ ->
                primitive.implementation.solve(request)
            }
    }
}

