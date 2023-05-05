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
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class SubSolveHandler(private val responseObserver: StreamObserver<GeneratorMsg>):
    ServerEvent<Struct, SubSolveResponse, Sequence<Solution>> {

    private val subSolvesMap: MutableMap<String, BlockingQueue<SubSolveResponse>> = mutableMapOf()

    override fun sendRequest(input: Struct): Sequence<Solution> {
        var id: String
        do {
            id = idGenerator()
        } while(subSolvesMap.keys.contains(id))
        subSolvesMap[id] = LinkedBlockingQueue()
        return sequence {
            var hasNext = true
            do {
                responseObserver.onNext(
                    buildSubSolveMsg(input, id)
                )
                val solution = subSolvesMap[id]!!.take().solution
                if(!solution.hasNext) hasNext = false
                yield(solution.deserialize(Scope.of(input)))
            } while(hasNext)
        }
    }

    override fun handleResponse(response: SubSolveResponse) {
        subSolvesMap[response.requestID]?.add(response)
    }
}