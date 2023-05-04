package it.unibo.tuprolog.primitives.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.primitive.Primitive
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import it.unibo.tuprolog.solve.primitive.Solve

class PrimitiveServerWrapper private constructor(val signature: Signature,
                                                 private val primitive: Primitive):
    GenericPrimitiveServiceGrpc.GenericPrimitiveServiceImplBase()  {

    override fun callPrimitive(responseObserver: StreamObserver<ResponseMsg>): StreamObserver<RequestMsg> {
        return object: StreamObserver<RequestMsg> {

            var stream: Iterator<Solve.Response>? = null

            override fun onNext(msg: RequestMsg) {
                try {
                    if (stream == null) {
                        stream = primitive.solve(msg.deserialize()).iterator()
                    }
                    responseObserver.onNext(
                        stream!!.next().serialize(stream!!.hasNext())
                    )
                    if (!stream!!.hasNext()) this.onCompleted()
                } catch (e: ResolutionException) {
                    stream = iterator {
                        e.serialize()
                    }
                }
            }

            override fun onError(t: Throwable?) {}

            override fun onCompleted() {
                responseObserver.onCompleted()
            }

        }
    }

    /** Respond with the signature of the primitive **/
    override fun getSignature(request: EmptyMsg?, responseObserver: StreamObserver<SignatureMsg>) {
        responseObserver.onNext(signature.serialize())
        responseObserver.onCompleted()
    }

    companion object {

        fun of(functor: String, arity: Int,
               primitive: Primitive): PrimitiveServerWrapper =
            PrimitiveServerWrapper(Signature(functor, arity), primitive)

        fun of(signature: Signature,
               primitive: Primitive): PrimitiveServerWrapper =
            PrimitiveServerWrapper(signature, primitive)

        fun from(primitiveWrapper: PrimitiveWrapper<ExecutionContext>): PrimitiveServerWrapper =
            PrimitiveServerWrapper(primitiveWrapper.signature, primitiveWrapper.implementation)
    }
}

