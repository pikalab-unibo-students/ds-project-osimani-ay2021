package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.primitives.SolutionMsg
import it.unibo.tuprolog.solve.Solution

fun Solution.serialize(): SolutionMsg {
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
    return solutionBuilder.build()
}

