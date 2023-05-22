package it.unibo.tuprolog.primitives.parsers.serializers.distribuited

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.Session
import it.unibo.tuprolog.solve.SolveOptions

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


