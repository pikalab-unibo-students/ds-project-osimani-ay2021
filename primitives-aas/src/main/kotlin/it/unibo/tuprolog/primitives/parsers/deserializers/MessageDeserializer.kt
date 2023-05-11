package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedRequest
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

fun RequestMsg.deserialize(): Solve.Request<ExecutionContext> =
    Solve.Request(
        this.signature.deserialize(),
        this.argumentsList.map { it.deserialize() },
        this.context.deserialize(),
        this.startTime,
        this.maxDuration
    )

fun RequestMsg.deserialize(session: ServerSession): DistribuitedRequest =
    DistribuitedRequest(
        this.signature.deserialize(),
        this.argumentsList.map { it.deserialize() },
        this.context.deserialize(),
        session
    )

fun ResponseMsg.deserialize(scope: Scope = Scope.empty()): Solve.Response =
    Solve.Response(
        solution = this.solution.deserialize(scope),
        sideEffects = this.sideEffectsList.map { it.deserialize() })

