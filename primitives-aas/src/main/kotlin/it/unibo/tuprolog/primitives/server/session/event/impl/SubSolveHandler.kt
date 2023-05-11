package it.unibo.tuprolog.primitives.server.session.event.impl

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SubSolveResponse
import it.unibo.tuprolog.primitives.idGenerator
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.session.event.ServerEvent
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

class SubSolveHandler(
    private val responseObserver: StreamObserver<GeneratorMsg>
): ServerEvent<Struct, SubSolveResponse, Sequence<Solution>> {

    private val subSolvesMap: MutableMap<String, BlockingQueue<SubSolveResponse>> = mutableMapOf()

    override fun sendRequest(input: Struct): Sequence<Solution> {
        val id = generateID()
        subSolvesMap.putIfAbsent(id, LinkedBlockingQueue(1))
        return object: Iterator<Solution> {
            private var available = true
            override fun hasNext(): Boolean = available
            override fun next(): Solution {
                responseObserver.onNext(
                    buildSubSolveMsg(input, id)
                )
                val solution = subSolvesMap[id]!!.take().solution
                available = solution.hasNext
                return solution.deserialize(Scope.of(input))
            }
        }.asSequence()
    }

    override fun handleResponse(response: SubSolveResponse) {
        subSolvesMap[response.requestID]?.add(response)
    }

    private fun generateID(): String {
        var id: String
        do {
            id = idGenerator()
        } while(subSolvesMap.keys.contains(id))
        return id
    }
}