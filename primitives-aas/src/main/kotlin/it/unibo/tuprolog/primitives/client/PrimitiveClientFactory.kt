package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.primitive.Primitive
import it.unibo.tuprolog.solve.primitive.Solve

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
        return signature.deserialize() to Primitive(primitive((channel)))
    }

    fun searchPrimitive(functor: String, arity: Int):
        Pair<Signature, Primitive> {
        val address = DbManager.get().getPrimitive(functor, arity)!!
        return connectToPrimitive(address.first, address.second)
    }

    fun searchPrimitive(signature: Signature): Pair<Signature, Primitive>  =
        searchPrimitive(signature.name, signature.arity)

    fun searchLibrary(libraryName: String): Library =
        Library.of(libraryName, DbManager.get().getLibrary(libraryName)
            .associate {
                searchPrimitive(it.first, it.second)
            })

    /** It returns the results from a [Solve.Request] given by the server mapping it into a lazy sequence of [Solve.Response]
     */
    private fun primitive(channel: ManagedChannel): (Solve.Request<ExecutionContext>) -> Sequence<Solve.Response> = {
        val observer = StreamedQueue(it, channel)
        sequence {
            while (!observer.isClosed) {
                yield(observer.popElement())
            }
        }
    }

}