package it.unibo.tuprolog.primitives.parsers.serializers.distribuited

import it.unibo.tuprolog.primitives.SolutionMsg
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedSolution

fun DistributedSolution.serialize(hasNext: Boolean = true): SolutionMsg {
    val solutionBuilder = SolutionMsg.newBuilder()
        .setQuery(query.serialize())
        .setType(
            if(isYes) SolutionMsg.SolutionType.SUCCESS
            else if(isNo) SolutionMsg.SolutionType.FAIL
            else SolutionMsg.SolutionType.HALT
        )
    if(substitution.isSuccess) {
        substitution.forEach {
            solutionBuilder.putSubstitutions(it.key.name,
                it.value.serialize())
        }
    }
    if(exception != null) {
        solutionBuilder.setError(exception!!.serialize())
    }
    solutionBuilder.setHasNext(hasNext)
    return solutionBuilder.build()
}

