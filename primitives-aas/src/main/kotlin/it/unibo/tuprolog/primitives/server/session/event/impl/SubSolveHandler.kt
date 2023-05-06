package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SubSolveResponse
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.session.event.ServerEvent
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.runBlocking
import java.util.concurrent.BlockingQueue
import kotlin.reflect.KSuspendFunction1

class SubSolveHandler(private val emit: KSuspendFunction1<GeneratorMsg, Unit>):
    ServerEvent<Struct, SubSolveResponse, Sequence<Solution>> {

    private val subSolvesMap: MutableMap<String, BlockingQueue<SubSolveResponse>> = mutableMapOf()
    private val ongoingRequests: MutableList<String> = mutableListOf()
    private val availableResponses: MutableMap<String, MutableList<SubSolveResponse>> = mutableMapOf()

    override suspend fun applyEvent(input: Struct): Sequence<Solution> {
        val id = idGenerator()
        ongoingRequests.add(id)
        return sequence {
            var hasNext = true
            do {
                runBlocking { emit(buildSubSolveMsg(input, id)) }
                val solution = subSolvesMap[id]!!.take().solution
                if (!solution.hasNext) hasNext = false
                yield(solution.deserialize(Scope.of(input)))
            } while (hasNext)
        }
    }

    override fun handleResponse(response: SubSolveResponse) {
        subSolvesMap[response.requestID]?.add(response)
    }

    private fun idGenerator(): String {
        var id: String
        do {
            id = idGenerator()
        } while(id in ongoingRequests.toTypedArray() || id in availableResponses.keys.toTypedArray())
        return id
    }
}