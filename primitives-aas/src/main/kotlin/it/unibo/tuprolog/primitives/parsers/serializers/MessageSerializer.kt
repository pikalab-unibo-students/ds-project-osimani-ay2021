package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.messages.*
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.channel.Channel
import it.unibo.tuprolog.solve.channel.ChannelStore
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.primitive.Solve
import it.unibo.tuprolog.unify.Unificator

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

fun buildLogicStackTraceResponse(id: String, content: List<Struct>): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setLogicStackTrace(
                LogicStacktraceMsg.newBuilder().addAllLogicStackTrace(
                    content.map { it.serialize() }
                )
            )
        )
    ).build()

fun buildCustomDataStoreResponse(id: String, content: CustomDataStore): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setCustomDataStore(
                content.serialize()
            )
        )
    ).build()


fun buildUnificatorResponse(id: String, content: Unificator): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setUnificator(
                content.serialize()
            )
        )
    ).build()

fun buildLibrariesResponse(id: String, content: Runtime): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setLibraries(
                content.serialize()
            )
        )
    ).build()

fun buildFlagStoreResponse(id: String, content: FlagStore): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setFlags(
                content.serialize()
            )
        )
    ).build()

fun buildOperatorsResponse(id: String, content: OperatorSet): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setOperators(
                content.serialize()
            )
        )
    ).build()

fun buildChannelResponse(id: String, names: List<String>): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setGenericGet(
            GenericGetResponse.newBuilder().setChannels(
                ChannelsMsg.newBuilder().addAllChannels(names)
            )
        )
    ).build()

fun buildSubSolveSolutionMsg(id: String, response: Solve.Response, hasNext: Boolean = true): SolverMsg =
    SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setSolution(
            response.serialize(hasNext)))
        .build()

fun buildClauseMsg(id: String, clause: Clause?): SolverMsg {
    return SolverMsg.newBuilder().setResponse(
        SubResponseMsg.newBuilder().setId(id).setClause(
            clause?.serialize()
        )
    ).build()
}


