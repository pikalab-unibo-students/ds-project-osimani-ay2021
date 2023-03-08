package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg.ClauseMsg
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*
import it.unibo.tuprolog.solve.lpaas.util.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.fromTheoryToMsg
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

internal class ClientPrologMutableSolverImpl(unificator: Map<String, String>, libraries: Set<String>,
                                             flags: Map<String, String>, staticKb: Theory, dynamicKb: Theory,
                                             operators: Map<String, Pair<String, Int>>, inputChannels: Map<String, String>,
                                             outputChannels: Set<String>, defaultBuiltins: Boolean):
    ClientPrologSolverImpl(unificator, libraries, flags, staticKb, dynamicKb,
        operators, inputChannels, outputChannels, defaultBuiltins), ClientMutableSolver {

    private val mutableSolverFutureStub: MutableSolverGrpc.MutableSolverFutureStub = MutableSolverGrpc
        .newFutureStub(channel)

    private val mutableSolverStub: MutableSolverGrpc.MutableSolverStub = MutableSolverGrpc
        .newStub(channel)

    private fun operationWithResult(op: () -> OperationResult) {
        val result = op()
        if (!result.result) println(result.error)
    }

    override fun loadLibrary(libraryName: String) {
        operationWithResult {
            mutableSolverFutureStub.loadLibrary(
                MutableLibrary.newBuilder().setSolverID(solverID)
                    .setLibrary(RuntimeMsg.LibraryMsg.newBuilder().setName(libraryName)).build()
            ).get()
        }
    }

    override fun unloadLibrary(libraryName: String) {
        operationWithResult {
            mutableSolverFutureStub.unloadLibrary(
                MutableLibrary.newBuilder().setSolverID(solverID)
                    .setLibrary(RuntimeMsg.LibraryMsg.newBuilder().setName(libraryName)).build()
            ).get()
        }
    }

    override fun setRuntime(libraries: Set<String>) {
        operationWithResult {
            mutableSolverFutureStub.setLibraries(
                MutableRuntime.newBuilder().setSolverID(solverID)
                    .setRuntime(fromLibrariesToMsg(libraries)).build()
            ).get()
        }
    }

    private fun sendTheoryAsStream(theory: Theory, operation: (StreamObserver<OperationResult>) -> StreamObserver<MutableClause>):
        OperationResult {
        val future: CompletableDeferred<OperationResult> = CompletableDeferred()
        val stream = operation(object : StreamObserver<OperationResult> {
            override fun onNext(value: OperationResult) {
                future.complete(value)
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        })

        theory.clauses.forEach {
            stream.onNext(fromClauseToMutableMsg(solverID, it))
        }
        stream.onCompleted()

        return runBlocking {
            future.await()
        }
    }

    override fun loadStaticKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.loadStaticKB(it)}
        }
    }

    override fun appendStaticKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.appendStaticKB(it)}
        }
    }

    override fun resetStaticKb() {
        operationWithResult {
            mutableSolverFutureStub.resetStaticKb(SolverID.newBuilder().setSolverID(solverID).build()).get()
        }
    }

    override fun loadDynamicKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.loadDynamicKB(it)}
        }
    }

    override fun appendDynamicKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.appendDynamicKB(it)}
        }
    }

    override fun resetDynamicKb() {
        operationWithResult {
            mutableSolverFutureStub.resetDynamicKb(SolverID.newBuilder().setSolverID(solverID).build()).get()
        }
    }

    override fun assertA(fact: Struct) {
        operationWithResult {
            mutableSolverFutureStub.assertA(fromClauseToMutableMsg(solverID, fact)).get()
        }
    }

    override fun assertZ(fact: Struct) {
        operationWithResult {
            mutableSolverFutureStub.assertZ(fromClauseToMutableMsg(solverID, fact)).get()
        }
    }

    override fun retract(fact: Struct): RetractResult<Theory> {
        TODO("Not yet implemented")
    }

    override fun retractAll(fact: Struct): RetractResult<Theory> {
        TODO("Not yet implemented")
    }

    override fun setFlag(name: String, value: Term) {
        operationWithResult {
            mutableSolverFutureStub.setFlag(MutableFlag.newBuilder().setSolverID(solverID)
                .setFlag(FlagsMsg.FlagMsg.newBuilder().setName(name).setValue(value.toString())).build()).get()
        }
    }

    private fun setChannel(name: String, content: String, type: MutableChannelID.CHANNEL_TYPE) {
        operationWithResult {
            mutableSolverFutureStub.setChannel(MutableChannelID.newBuilder().setSolverID(solverID)
                .setType(type)
                .setChannel(Channels.ChannelID.newBuilder().setName(name).setContent(content)).build()).get()
        }
    }

    override fun setStandardInput(name: String, content: String) {
        setChannel(name, content, MutableChannelID.CHANNEL_TYPE.INPUT)
    }

    override fun setStandardError(name: String) {
        setChannel(name, "", MutableChannelID.CHANNEL_TYPE.ERROR)
    }

    override fun setStandardOutput(name: String) {
        setChannel(name, "", MutableChannelID.CHANNEL_TYPE.OUTPUT)
    }

    override fun setWarnings(name: String) {
        setChannel(name, "", MutableChannelID.CHANNEL_TYPE.WARNING)
    }
}