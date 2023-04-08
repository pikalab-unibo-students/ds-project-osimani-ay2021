package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.DummyInstances
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.lpaas.MutableSolverGrpc
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableClause
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableFlag
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableRuntime
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.RetractResultMsg
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.MutableChannelID
import it.unibo.tuprolog.solve.lpaas.server.channels.ChannelsDequesCollector
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.MessageBuilder
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parse
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parseToClause
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toFlagMsg
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class ClientPrologMutableSolverImpl(solverID: String, channel: ManagedChannel) :
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
                MessageBuilder.fromLibraryToMutableMsg(solverID, libraryName)
            ).get()
        }
    }

    override fun unloadLibrary(libraryName: String) {
        operationWithResult {
            mutableSolverFutureStub.unloadLibrary(
                MessageBuilder.fromLibraryToMutableMsg(solverID, libraryName)
            ).get()
        }
    }

    override fun setRuntime(libraries: Set<String>) {
        operationWithResult {
            mutableSolverFutureStub.setLibraries(
                MutableRuntime.newBuilder().setSolverID(solverID)
                    .setRuntime(
                        RuntimeMsg.newBuilder().addAllLibraries(
                            libraries.map { RuntimeMsg.LibraryMsg.newBuilder().setName(it).build()})).build()
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

        if (theory.isEmpty) {
            stream.onNext(MutableClause.newBuilder().setSolverID(solverID).build())
        } else {
            theory.clauses.forEach {
                stream.onNext(MessageBuilder.fromClauseToMutableMsg(solverID, it))
            }
        }
        stream.onCompleted()

        return runBlocking {
            future.await()
        }
    }

    override fun loadStaticKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.loadStaticKB(it) }
        }
    }

    override fun appendStaticKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.appendStaticKB(it) }
        }
    }

    override fun resetStaticKb() {
        operationWithResult {
            mutableSolverFutureStub.resetStaticKb(SolverID.newBuilder().setSolverID(solverID).build()).get()
        }
    }

    override fun loadDynamicKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.loadDynamicKB(it) }
        }
    }

    override fun appendDynamicKb(theory: Theory) {
        operationWithResult {
            sendTheoryAsStream(theory) { mutableSolverStub.appendDynamicKB(it) }
        }
    }

    override fun resetDynamicKb() {
        operationWithResult {
            mutableSolverFutureStub.resetDynamicKb(SolverID.newBuilder().setSolverID(solverID).build()).get()
        }
    }

    override fun assertA(fact: Struct) {
        operationWithResult {
            mutableSolverFutureStub.assertA(MessageBuilder.fromClauseToMutableMsg(solverID, fact)).get()
        }
    }

    override fun assertZ(fact: Struct) {
        operationWithResult {
            mutableSolverFutureStub.assertZ(MessageBuilder.fromClauseToMutableMsg(solverID, fact)).get()
        }
    }

    override fun retract(fact: Struct): RetractResult<Theory> {
        return genericRetract { mutableSolverFutureStub.retract(MessageBuilder.fromClauseToMutableMsg(solverID, fact)).get() }
    }

    override fun retractAll(fact: Struct): RetractResult<Theory> {
        return genericRetract { mutableSolverFutureStub.retractAll(MessageBuilder.fromClauseToMutableMsg(solverID, fact)).get() }
    }

    private fun genericRetract(op: () -> RetractResultMsg): RetractResult<Theory> {
        val result = op()
        val theory = Theory.of(
            result.theory.parse()
        )
        val clauses = result.clausesList.map {
            it.parseToClause()
        }
        return if(result.isSuccess) {
            RetractResult.Success(theory, clauses)
        } else RetractResult.Failure(theory)
    }

    override fun setFlag(name: String, value: Term) {
        operationWithResult {
            mutableSolverFutureStub.setFlag(
                MutableFlag.newBuilder().setSolverID(solverID)
                .setFlag(Pair(name, value).toFlagMsg()).build()).get()
        }
    }

    override fun setStandardInput(content: String) {
        operationWithResult {
            mutableSolverFutureStub.setChannel(MutableChannelID.newBuilder().setSolverID(solverID)
                .setType(MutableChannelID.CHANNEL_TYPE.INPUT)
                .setChannel(MessageBuilder.fromChannelIDToMsg("", content.toCharArray()
                    .map { it.toString() })).build()).get()
        }
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
            .setChannelID(MessageBuilder.fromChannelIDToMsg(type))
            .setSolverID(solverID).build())
        openStreamObservers.add(stub)
    }
}
