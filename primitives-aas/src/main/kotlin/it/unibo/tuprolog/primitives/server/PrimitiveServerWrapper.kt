package it.unibo.tuprolog.primitives.server

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpcKt
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.primitives.server.session.SessionObserver
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PrimitiveServerWrapper private constructor(val signature: Signature,
                                                 private val primitive: PrimitiveWithSession
):
    GenericPrimitiveServiceGrpcKt.GenericPrimitiveServiceCoroutineImplBase()  {


    override fun callPrimitive(requests: Flow<SolverMsg>): Flow<GeneratorMsg> {
        var stream: Iterator<Solve.Response>? = null
        return flow{
            requests.collect {msg ->
                val session = SessionObserver(::emit)
                when (stream) {
                    null -> {
                        /** Handling Initialization Event */
                        if (msg.hasRequest()) {
                            stream = primitive.solve(msg.request.deserialize(), session).iterator()
                        } else {
                            println("ERROR, STREAM IS NOT INITIALIZED")
                        }
                    }
                    else -> {
                        /** Handling Next Event */
                        if (msg.hasNext()) {
                            val solution = stream!!.next().serialize(stream!!.hasNext())
                            emit(GeneratorMsg.newBuilder().setResponse(solution).build())
                        }
                        /** Handling SubSolve Solution Event */
                        else if (msg.hasSolution()) {
                            session.addSolution(msg.solution)
                        }
                        /** Handling ReadLine Response Event */
                        else if (msg.hasLine()) {
                            session.addLine(msg.line)
                        }
                        /** Throws error if it tries to initialize again */
                        else {
                           throw IllegalArgumentException()
                        }
                    }
                }
            }
        }
    }

    override suspend fun getSignature(request: EmptyMsg): SignatureMsg {
        return signature.serialize()
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

