package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
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

fun DistributedResponse.serialize(hasNext: Boolean = true): ResponseMsg =
    ResponseMsg.newBuilder()
        .setSolution(solution.serialize(hasNext))
        .addAllSideEffects(sideEffects.map { it.serialize() })
        .build()

fun buildSubSolveMsg(query: Struct, id: String,
                     lazy: Boolean = true,
                     timeout: Long = SolveOptions.MAX_TIMEOUT,
                     limit: Int = SolveOptions.ALL_SOLUTIONS): GeneratorMsg =
    GeneratorMsg.newBuilder().setRequest(
        SubRequestMsg.newBuilder().setId(id).setSubSolve(
            SubSolveRequest.newBuilder().setQuery(query.serialize())
                .setLazy(lazy).setTimeout(timeout).setLimit(limit))
    ).build()

fun buildReadLineMsg(id: String, channelName: String): GeneratorMsg =
    GeneratorMsg.newBuilder().setRequest(
        SubRequestMsg.newBuilder().setId(id).setReadLine(
            ReadLineMsg.newBuilder().setChannelName(channelName))
    ).build()

fun buildLineMsg(id: String, channelName: String, line: String): SolverMsg {
    val builder = LineMsg.newBuilder().setChannelName(channelName).setContent(line)
    return SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setLine(
            if (line.isBlank()) builder.setFailed(true)
            else builder.setContent(line))
    ).build()
}

fun buildSubSolveSolutionMsg(id: String, solution: Solution, hasNext: Boolean = true): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setSolution(
            solution.serialize(hasNext)))
        .build()


