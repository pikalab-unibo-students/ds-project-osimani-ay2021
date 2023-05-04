package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

fun Solve.Request<ExecutionContext>.serialize(): RequestMsg =
    RequestMsg.newBuilder()
        .setSignature(this.signature.serialize())
        .addAllArguments(this.arguments.map { it.serialize() })
        .setContext(this.context.serialize())
        .setStartTime(this.startTime)
        .setMaxDuration(this.maxDuration).build()

fun Solve.Response.serialize(hasNext: Boolean = true): ResponseMsg =
    ResponseMsg.newBuilder()
        .setSolution(solution.serialize(hasNext))
        .addAllSideEffects(sideEffects.map { it.serialize() })
        .build()

