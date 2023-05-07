package it.unibo.tuprolog.primitives.server.session.impl

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineHandler
import it.unibo.tuprolog.primitives.server.session.event.impl.SubSolveHandler
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.coroutineContext

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ServerSessionImpl(
    private val responseObserver: StreamObserver<GeneratorMsg>,
    private val primitive: PrimitiveWithSession
): ServerSession {

    private var stream: Iterator<Solve.Response>? = null

    override fun onNext(msg: SolverMsg) {
        when (stream) {
            null -> {
                if (msg.hasRequest()) {
                    stream = primitive.solve(msg.request.deserialize(), this).iterator()
                } else {
                    println("ERROR, STREAM IS NOT INITIALIZED")
                }
            }
            else -> {
                Thread {
                    handleEvent(msg)
                }.start()
            }
        }
    }

    override fun onError(t: Throwable?) {
        println("from server $t")
    }

    override fun onCompleted() {
        responseObserver.onCompleted()
    }

    private val subSolveHandler: SubSolveHandler = SubSolveHandler(responseObserver)
    private val readLineHandler: ReadLineHandler = ReadLineHandler(responseObserver)

    private fun handleEvent(event: SolverMsg) {
        /** Handling Next Request */
        if(event.hasNext()) {
            if(stream != null) {
                try {
                    val solution = stream!!.next().serialize(stream!!.hasNext())
                    responseObserver.onNext(
                        GeneratorMsg.newBuilder().setResponse(solution).build()
                    )
                    if (!stream!!.hasNext()) this.onCompleted()
                } catch (e: Exception) {
                    println("error: $e")
                }
            }
        }
        /** Handling SubSolve Solution Event */
        else if(event.hasSolution()) {
            subSolveHandler.handleResponse(event.solution)
        }
        /** Handling ReadLine Response Event */
        else if(event.hasLine()) {
            readLineHandler.handleResponse(event.line)
        }
        /** Throws error if it tries to initialize again */
        else if(event.hasRequest()) {
            println("STREAM IS ALREADY INITIALIZED")
        }
    }

    override fun subSolve(query: Struct): Sequence<Solution> = subSolveHandler.sendRequest(query)

    override fun readLine(channelName: String): String = readLineHandler.sendRequest(channelName)
}