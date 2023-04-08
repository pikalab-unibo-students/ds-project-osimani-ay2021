package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.SolverFactoryGrpc
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverId
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverRequest
import it.unibo.tuprolog.solve.lpaas.util.parsers.MessageBuilder
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toMsg
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator


object ClientPrologSolverFactory  {

    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()
    private val stub = SolverFactoryGrpc.newFutureStub(channel)

    fun solverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                 flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                 dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                 outputs: Map<String, String> = emptyMap(), defaultBuiltins: Boolean = true): ClientSolver {
        return ClientPrologSolverImpl(
            generateSolverID(unificator, libraries, flags, staticKb, dynamicKb,
            inputs, outputs, defaultBuiltins), channel
        )
    }

    fun mutableSolverOf(unificator: Unificator = Unificator.strict(), libraries: Set<String> = emptySet(),
                        flags: FlagStore = FlagStore.empty(), staticKb: Theory = Theory.empty(),
                        dynamicKb: Theory = Theory.empty(), inputs: Map<String, String> = emptyMap(),
                        outputs: Map<String, String> = emptyMap(), defaultBuiltins: Boolean = true): ClientMutableSolver {
        return ClientPrologMutableSolverImpl(
            generateSolverID(unificator, libraries, flags, staticKb, dynamicKb,
                inputs, outputs, defaultBuiltins, true), channel
        )
    }

    fun connectToSolver(solverId: String): ClientSolver? {
        val result = stub.connectToSolver(SolverId.newBuilder().setId(solverId).build()).get()
        return if(result.result) {
            ClientPrologSolverImpl(solverId, channel)
        } else {
            null
        }
    }

    /**Add check if its is mutable*/
    fun connectToMutableSolver(solverId: String): ClientMutableSolver? {
        val result = stub.connectToSolver(SolverId.newBuilder().setId(solverId).build()).get()
        return if(result.result) {
            ClientPrologMutableSolverImpl(solverId, channel)
        } else {
            null
        }
    }

    private fun generateSolverID(unificator: Unificator, libraries: Set<String>,
                                     flags: FlagStore, staticKb: Theory, dynamicKb: Theory,
                                     inputChannels: Map<String, String>, outputChannels: Map<String, String>,
                                     defaultBuiltins: Boolean, mutable: Boolean = false): String {
        val createSolverRequest: SolverRequest = SolverRequest.newBuilder()
            .setUnificator(unificator.toMsg())
            .setRuntime(MessageBuilder.fromLibrariesToMsg(libraries))
            .setFlags(flags.toMsg())
            .setStaticKb(staticKb.toMsg())
            .setDynamicKb(dynamicKb.toMsg())
            .setInputStore(MessageBuilder.fromChannelsToMsg(inputChannels))
            .setOutputStore(MessageBuilder.fromChannelsToMsg(outputChannels))
            .setDefaultBuiltIns(defaultBuiltins)
            .setMutable(mutable).build()
        return stub.solverOf(createSolverRequest).get().id
    }
}
