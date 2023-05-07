package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SubSolveResponse
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.session.event.ServerEvent
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.concurrent.BlockingQueue
import kotlin.reflect.KSuspendFunction1

class SubSolveHandler(private val emit: KSuspendFunction1<GeneratorMsg, Unit>):
    ServerEvent<Struct, SubSolveResponse, Flow<Solution>> {

    private val subSolvesMap: MutableMap<String, Channel<SubSolveResponse>> = mutableMapOf()
    private val ongoingRequests: MutableList<String> = mutableListOf()
    private val availableResponses: MutableMap<String, MutableList<SubSolveResponse>> = mutableMapOf()

    override suspend fun applyEvent(input: Struct): Flow<Solution> {
        val id = idGenerator()
        ongoingRequests.add(id)
        return flow {
            var hasNext = true
            do {
                emit(buildSubSolveMsg(input, id))
                val solution = subSolvesMap[id]!!.receive().solution
                if (!solution.hasNext) hasNext = false
                this.emit(solution.deserialize(Scope.of(input)))
            } while (hasNext)
        }
    }

    override suspend fun handleResponse(response: SubSolveResponse) {
        subSolvesMap[response.requestID]?.send(response)
    }

    private fun idGenerator(): String {
        var id: String
        do {
            id = idGenerator()
        } while(id in ongoingRequests.toTypedArray() || id in availableResponses.keys.toTypedArray())
        return id
    }
}