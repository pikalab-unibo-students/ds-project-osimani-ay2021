package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.messages.TheoryMsg
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
        .setSolution(this.solution.serialize(hasNext))
        .addAllSideEffects(sideEffects.map { it.serialize() })
        .build()

fun buildLineMsg(id: String, channelName: String, line: String): SolverMsg {
    val builder = LineMsg.newBuilder().setChannelName(channelName).setContent(line)
    return SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setLine(
            if (line.isBlank()) builder.setFailed(true)
            else builder.setContent(line))
    ).build()
}

fun buildSubSolveSolutionMsg(id: String, response: Solve.Response, hasNext: Boolean = true): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setSolution(
            response.serialize(hasNext)))
        .build()

fun buildTheoryMsg(id: String, clauses: Iterable<Clause>): SolverMsg {
    return SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setTheory(
            TheoryMsg.newBuilder().addAllClauses(clauses.map { it.serialize() })
        )
    ).build()
}


