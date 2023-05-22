package it.unibo.tuprolog.primitives.parsers.deserializers.distribuited

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.messages.ExecutionContextMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.deserializers.deserializeVar
import it.unibo.tuprolog.primitives.server.distribuited.DistributedExecutionContext
import it.unibo.tuprolog.primitives.server.session.ContextRequester

fun ExecutionContextMsg.deserializeAsDistributed(
    scope: Scope = Scope.empty(),
    contextRequester: ContextRequester
): DistributedExecutionContext =
    DistributedExecutionContext(
        procedure = this.procedure.deserialize(scope),
        substitution = Substitution.of(this.substitutionsMap.map {
            Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
        }.toMap()),
        startTime = this.startTime,
        endTime = this.endTime,
        remainingTime = this.remainingTime,
        elapsedTime = this.elapsedTime,
        maxDuration = this.maxDuration,
        contextRequester = contextRequester
    )





