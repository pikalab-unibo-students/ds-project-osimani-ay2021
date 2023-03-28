package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.DummyInstances
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.*
import it.unibo.tuprolog.solve.lpaas.server.collections.ChannelsDequesCollector
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.fromTheoryToMsg
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.*
import java.util.concurrent.LinkedBlockingDeque

class ClientPrologMutableSolverImpl(solverID: String,channel: ManagedChannel):
    ClientPrologSolverImpl(solverID, channel), ClientMutableSolver {

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
                fromLibraryToMutableMsg(solverID, libraryName)
            ).get()
        }
    }

    override fun unloadLibrary(libraryName: String) {
        operationWithResult {
            mutableSolverFutureStub.unloadLibrary(
                fromLibraryToMutableMsg(solverID, libraryName)
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

        if(theory.isEmpty) {
            stream.onNext(MutableClause.newBuilder().setSolverID(solverID).build())
        } else {
            theory.clauses.forEach {
                stream.onNext(fromClauseToMutableMsg(solverID, it))
            }
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
        return genericRetract { mutableSolverFutureStub.retract(fromClauseToMutableMsg(solverID, fact)).get() }
    }

    override fun retractAll(fact: Struct): RetractResult<Theory> {
        return genericRetract { mutableSolverFutureStub.retractAll(fromClauseToMutableMsg(solverID, fact)).get() }
    }

    private fun genericRetract(op: () -> RetractResultMsg): RetractResult<Theory> {
        val result = op()
        val theory = Theory.of(result.theory.clauseList.map {
            deserializer.deserialize(it.content).castToClause() })
        val clauses = result.clausesList.map {
            deserializer.deserialize(it.content).castToClause() }
        return if(result.isSuccess) {
            RetractResult.Success(theory, clauses)
        } else RetractResult.Failure(theory)
    }

    override fun setFlag(name: String, value: Term) {
        operationWithResult {
            mutableSolverFutureStub.setFlag(MutableFlag.newBuilder().setSolverID(solverID)
                .setFlag(fromFlagToMsg(name, value)).build()).get()
        }
    }

    private fun setChannel(content: String, type: MutableChannelID.CHANNEL_TYPE) {
        operationWithResult {
            mutableSolverFutureStub.setChannel(MutableChannelID.newBuilder().setSolverID(solverID)
                .setType(type)
                .setChannel(fromChannelIDToMsg("", content)).build()).get()
        }
    }

    override fun setStandardInput(content: String) {
        setChannel(content, MutableChannelID.CHANNEL_TYPE.INPUT)
    }

    override fun setStandardOutput(stdOut: OutputChannel<String>) {
        setOutChannel(OutputStore.STDOUT) { stdOut.write(it) }
    }

    override fun setStandardError(stdErr: OutputChannel<String>) {
       setOutChannel(OutputStore.STDERR) { stdErr.write(it) }
    }

    override fun setWarnings(stdWarn: OutputChannel<Warning>) {
        setOutChannel(ChannelsDequesCollector.STDWARN) { stdWarn.write(
            object: Warning(it, null, DummyInstances.executionContext) {
                //does nothing
                override fun updateContext(newContext: ExecutionContext, index: Int): Warning = this
                //does nothing
                override fun updateLastContext(newContext: ExecutionContext): Warning = this
                //does nothing
                override fun pushContext(newContext: ExecutionContext): Warning = this
        })}
    }

    private fun setOutChannel(type: String, op: (String)->Unit) {
        val stub = solverStub.readStreamFromOutputChannel(object: StreamObserver<ReadLine> {
            override fun onNext(value: ReadLine) {
                op(value.line)
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        })
        stub.onNext(OutputChannelEvent.newBuilder()
            .setChannelID(fromChannelIDToMsg(type))
            .setSolverID(solverID).build())
        openStreamObservers.add(stub)
    }
}