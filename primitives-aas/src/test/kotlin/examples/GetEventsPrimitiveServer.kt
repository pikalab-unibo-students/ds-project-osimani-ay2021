package examples

import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.Solver

val getEventsPrimitive = DistributedPrimitiveWrapper("testEvents", 0) { request ->
    sequence {
        val log = mutableListOf<String>()
        var flag = true

        listOf(
            Pair(Solver.prolog.defaultUnificator.context, request.context.unificator.context),
            Pair(Solver.prolog.defaultFlags, request.context.flags),
            Pair(Solver.prolog.defaultStaticKb, request.context.staticKb),
            Pair(Solver.prolog.defaultDynamicKb, request.context.dynamicKb)
        ).forEach { pair ->
            if(pair.first != pair.second) {
                log.add(printDifference(pair.second, pair.first))
                flag = false
            }
        }

        if(flag)
            yield(request.replySuccess())
        else
            yield(request.replyError(DistributedError.ResolutionException(log.toString())))
    }
}

private fun printDifference(actual: Any, expected: Any): String =
    "$actual was received instead of $expected"

fun main() {
    startService(getEventsPrimitive, 8086, "customLibrary")
}