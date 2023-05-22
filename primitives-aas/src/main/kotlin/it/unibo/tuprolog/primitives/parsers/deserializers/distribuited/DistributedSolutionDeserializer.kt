package it.unibo.tuprolog.primitives.parsers.deserializers.distribuited

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.SolutionMsg
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedSolution

fun SolutionMsg.deserializeAsDistributed(scope: Scope = Scope.empty()): DistributedSolution {
    val query = this.query.deserialize(scope)
    return when(this.type) {
        SolutionMsg.SolutionType.SUCCESS -> {
            val substitution = Substitution.of(this.substitutionsMap.map {
                Pair(scope.varOf(it.key),
                    it.value.deserialize(scope))
            }).asUnifier()
            if (substitution != null) {
                DistributedSolution.yes(query, substitution)
            }
            else DistributedSolution.yes(query)
        }
        SolutionMsg.SolutionType.FAIL -> {
            DistributedSolution.no(query)
        }
        SolutionMsg.SolutionType.HALT -> {
            DistributedSolution.halt(query, this.error.deserializeAsDistributed(scope))
        }
        else ->
            throw ParsingException(this)
    }
}