package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.primitive.Solve
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ConnectionObserver(
    private val responseObserver: StreamObserver<GeneratorMsg>,
    private val primitive: PrimitiveWithSession
): StreamObserver<SolverMsg>, Session {


    private var stream: Iterator<Solve.Response>? = null

    override fun onNext(msg: SolverMsg) {
        when(stream) {
            null -> {
                if (msg.hasRequest()) {
                    stream = primitive.solve(msg.request.deserialize(), this).iterator()
                } else {
                    println("ERROR, STREAM IS NOT INITIALIZED")
                }
            }
            else -> {
                handleEvent(msg)
            }
        }
    }

    override fun onError(t: Throwable?) {}

    override fun onCompleted() {
        responseObserver.onCompleted()
    }

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
            handleSubSolveResponse(event.solution)
        }
        /** Handling ReadLine Response Event */
        else if(event.hasLine()) {
            handleReadLineResponse(event.line)
        }
        /** Throws error if it tries to initialize again */
        else if(event.hasRequest()) {
            println("STREAM IS ALREADY INITIALIZED")
        }
    }

    private val subSolvesQueue: BlockingQueue<SolutionMsg> = LinkedBlockingQueue()

    private fun handleSubSolveResponse(response: SolutionMsg) {
        subSolvesQueue.add(response)
    }

    override fun subSolve(query: Struct): Sequence<Solution> {
        return sequence {
            var hasNext = true
            do {
                responseObserver.onNext(
                    GeneratorMsg.newBuilder().setSubSolve(
                        SubSolveMsg.newBuilder().setQuery(query.serialize())
                    ).build()
                )
                val solution = subSolvesQueue.take()
                if(!solution.hasNext) hasNext = false
                yield(solution.deserialize(Scope.of(query)))
            } while(hasNext)
        }
    }

    private val readLinesQueue: BlockingQueue<String> = LinkedBlockingQueue()

    private fun handleReadLineResponse(response: LineMsg) {
        readLinesQueue.add(response.content)
    }

    override fun readLine(channelName: String): String {
        responseObserver.onNext(
            GeneratorMsg.newBuilder().setReadLine(
                ReadLineMsg.newBuilder().setChannelName(channelName)
            ).build()
        )
        return readLinesQueue.take()
    }
}