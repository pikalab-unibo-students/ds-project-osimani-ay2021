package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.GenericGetMsg
import it.unibo.tuprolog.primitives.GenericGetResponse
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.buildGetMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

abstract class GetEvent <T: Any>(
    final override val id: String,
    type: GenericGetMsg.Element
): SubRequestEvent {

    override val message: GeneratorMsg = buildGetMsg(id, type)

    val result: CompletableDeferred<GenericGetResponse> = CompletableDeferred()

    override fun awaitResult(): T {
        return handleResult(runBlocking {
            result.await()
        })
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if(msg.hasGenericGet())
            this.result.complete(msg.genericGet)
        else
            throw IllegalArgumentException()
    }

    abstract fun handleResult(msg: GenericGetResponse): T

    companion object {

        fun ofLogicStackTrace(id: String) =
            object : GetEvent<List<Struct>>(id, GenericGetMsg.Element.LOGIC_STACKTRACE) {
                override fun handleResult(msg: GenericGetResponse): List<Struct> =
                    msg.logicStackTrace.logicStackTraceList.map {
                        it.deserialize()
                    }
            }

        fun ofCustomDataStore(id: String) =
            object : GetEvent<CustomDataStore>(id, GenericGetMsg.Element.CUSTOM_DATA_STORE) {
                override fun handleResult(msg: GenericGetResponse): CustomDataStore =
                    msg.customDataStore.deserialize()
            }

        fun ofUnificator(id: String) =
            object : GetEvent<Unificator>(id, GenericGetMsg.Element.UNIFICATOR) {
                override fun handleResult(msg: GenericGetResponse): Unificator =
                    msg.unificator.deserialize()
            }

        //To Improve
        fun ofLibraries(id: String) =
            object : GetEvent<DistributedRuntime>(id, GenericGetMsg.Element.LIBRARIES) {
                override fun handleResult(msg: GenericGetResponse): DistributedRuntime =
                    msg.libraries.deserialize()
            }

        fun ofFlagStore(id: String) =
            object : GetEvent<FlagStore>(id, GenericGetMsg.Element.FLAGS) {
                override fun handleResult(msg: GenericGetResponse): FlagStore =
                    msg.flags.deserialize()
            }

        fun ofOperators(id: String) =
            object : GetEvent<OperatorSet>(id, GenericGetMsg.Element.OPERATORS) {
                override fun handleResult(msg: GenericGetResponse): OperatorSet =
                    msg.operators.deserialize()
            }

        fun ofInputChannels(id: String) =
            object : GetEvent<Set<String>>(id, GenericGetMsg.Element.INPUT_CHANNELS) {
                override fun handleResult(msg: GenericGetResponse): Set<String> =
                    msg.channels.channelsList.toSet()
            }

        fun ofOutputChannels(id: String) =
            object : GetEvent<Set<String>>(id, GenericGetMsg.Element.OUTPUT_CHANNELS) {
                override fun handleResult(msg: GenericGetResponse): Set<String> =
                    msg.channels.channelsList.toSet()
            }
    }
}