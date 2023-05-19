package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.Session
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

fun buildInspectKbMsg(
    id: String,
    kbType: Session.KbType,
    maxClauses: Long,
    vararg filters: Pair<Session.KbFilter, String>
): GeneratorMsg =
    GeneratorMsg.newBuilder().setRequest(
        SubRequestMsg.newBuilder().setId(id).setInspectKb(
            InspectKbMsg.newBuilder()
                .setKbType(
                    when(kbType) {
                        Session.KbType.STATIC -> InspectKbMsg.KbType.STATIC
                        Session.KbType.DYNAMIC -> InspectKbMsg.KbType.DYNAMIC
                    })
                .setMaxClauses(maxClauses)
                .addAllFilters(filters.map {
                    InspectKbMsg.FilterMsg.newBuilder()
                        .setType(
                            when(it.first) {
                                Session.KbFilter.CONTAINS_TERM -> InspectKbMsg.FilterType.CONTAINS_TERM
                                Session.KbFilter.CONTAINS_FUNCTOR -> InspectKbMsg.FilterType.CONTAINS_FUNCTOR
                                Session.KbFilter.STARTS_WITH -> InspectKbMsg.FilterType.STARTS_WITH
                            })
                        .setArgument(it.second).build()}))
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

fun buildTheoryMsg(id: String, clauses: Iterable<Clause>): SolverMsg {
    return SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setTheory(
            TheoryMsg.newBuilder().addAllClauses(clauses.map { it.serialize() })
        )
    ).build()
}


