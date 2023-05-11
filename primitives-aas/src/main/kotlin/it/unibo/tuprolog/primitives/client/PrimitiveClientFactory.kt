package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.currentTimeInstant
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.primitive.Primitive
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.solve.primitive.UnaryPredicate
import it.unibo.tuprolog.solve.stdlib.primitive.Natural
import kotlinx.coroutines.flow.asFlow

/** The factory that creates a primitive given the URL of its server **/
object PrimitiveClientFactory {

    /** Connects to the primitive server and maps it to a local primitive
     * @return the primitive mapping of the connection
     */
    fun connectToPrimitive(address: String = "localhost", port: Int = 8080):
        Pair<Signature, Primitive> {
        val channel = ManagedChannelBuilder.forAddress(address, port)
            .usePlaintext()
            .build()
        val signature = GenericPrimitiveServiceGrpc.newFutureStub(channel)
            .getSignature(EmptyMsg.getDefaultInstance()).get()
        return signature.deserialize() to Primitive(primitive(channel))
    }

    /** It returns the results from a [Solve.Request] given by the server mapping it into
     * a lazy sequence of [Solve.Response]
     */
    private fun primitive(channel: ManagedChannel): (Solve.Request<ExecutionContext>) -> Sequence<Solve.Response> = {
        ClientSession.of(it, channel).solutionsQueue.asSequence()
    }

    fun searchPrimitive(functor: String, arity: Int):
        Pair<Signature, Primitive> {
        val address = DbManager.get().getPrimitive(functor, arity)!!
        return connectToPrimitive(address.first, address.second)
    }

    fun searchPrimitive(signature: Signature): Pair<Signature, Primitive> =
        searchPrimitive(signature.name, signature.arity)

    fun searchLibrary(libraryName: String): Library =
        Library.of(libraryName, DbManager.get().getLibrary(libraryName)
            .associate {
                searchPrimitive(it.first, it.second)
            })
}