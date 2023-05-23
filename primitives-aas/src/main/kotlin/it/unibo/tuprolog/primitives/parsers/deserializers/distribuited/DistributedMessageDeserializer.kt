package it.unibo.tuprolog.primitives.parsers.deserializers.distribuited

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedRequest
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.ServerSession

fun RequestMsg.deserializeAsDistributed(session: ServerSession): DistributedRequest =
    DistributedRequest(
        this.signature.deserialize(),
        this.argumentsList.map { it.deserialize() },
        this.context.deserializeAsDistributed(contextRequester = session),
        session
    )

fun ResponseMsg.deserializeAsDistributed(scope: Scope = Scope.empty()): DistributedResponse =
    DistributedResponse(
        solution = this.solution.deserializeAsDistributed(scope),
        sideEffects = this.sideEffectsList.map { it.deserialize() }
    )


