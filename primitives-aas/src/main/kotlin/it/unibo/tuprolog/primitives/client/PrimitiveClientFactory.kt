package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.primitives.db.DbManager
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.primitive.Primitive
import it.unibo.tuprolog.solve.primitive.Solve
import java.util.concurrent.TimeUnit

/** The factory that creates a primitive given the URL of its server **/
object PrimitiveClientFactory {

    /** Connects to the primitive server and maps it to a local primitive
     * @return the primitive mapping of the connection
     */
    fun connectToPrimitive(address: String = "localhost", port: Int = 8080):
        Pair<Signature, Primitive> {
        val channelBuilder = ManagedChannelBuilder.forAddress(address, port)
            .usePlaintext()
        val channel = channelBuilder.build()
        val signature = GenericPrimitiveServiceGrpc.newFutureStub(channel)
            .getSignature(EmptyMsg.getDefaultInstance()).get()
        channel.shutdown()
        channel.awaitTermination(5, TimeUnit.SECONDS)
        return signature.deserialize() to Primitive(primitive(channelBuilder))
    }

    /** It returns the results from a [Solve.Request] given by the server mapping it into
     * a lazy sequence of [Solve.Response]
     */
    private fun primitive(
        channelBuilder: ManagedChannelBuilder<*>
    ): (Solve.Request<ExecutionContext>) -> Sequence<Solve.Response> = {
        ClientSession.of(it, channelBuilder).solutionsQueue.asSequence()
    }

    /** It searches in a Database the information about a specific primitive
     */
    fun searchPrimitive(functor: String, arity: Int):
        Pair<Signature, Primitive> {
        val address = DbManager.get().getPrimitive(functor, arity)!!
        return connectToPrimitive(address.first, address.second)
    }

    fun searchLibrary(libraryName: String): Library =
        Library.of(libraryName, DbManager.get().getLibrary(libraryName)
            .associate {
                searchPrimitive(it.first, it.second)
            })
}