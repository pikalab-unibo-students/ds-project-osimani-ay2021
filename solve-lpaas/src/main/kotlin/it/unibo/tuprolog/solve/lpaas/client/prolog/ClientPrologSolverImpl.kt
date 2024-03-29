package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermSerializer
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg.ClauseMsg
import it.unibo.tuprolog.solve.lpaas.util.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.MessageBuilder
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parse
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parseToClause
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

open class ClientPrologSolverImpl(protected var solverID: String, protected var channel: ManagedChannel,
                                  outputChannels: Map<String, (String) -> Unit> = emptyMap()
):
    ClientSolver {

    private val solverStub: SolverGrpc.SolverStub = SolverGrpc.newStub(channel)
    private val solverFutureStub: SolverGrpc.SolverFutureStub = SolverGrpc.newFutureStub(channel)
    private val openStreamObservers: MutableList<StreamObserver<*>> = mutableListOf()

    init {
        outputChannels.forEach {
            setOutChannel(it.key, it.value)
        }
    }

    override fun getId(): String {
        return solverID
    }

    override fun closeClient(withDeletion: Boolean) {
        if(withDeletion) {
            solverFutureStub.deleteSolver(buildSolverId()).get()
        }
        openStreamObservers.forEach {
            try {
                it.onCompleted()
            } catch (_: Exception) {} }
    }

    override fun solve(goal: Struct, options: SolveOptions): SolutionsSequence {
        val reply = solverFutureStub.solve(buildRequestWithOptionsMessage(goal, options)).get()
        if(solverID != reply.solverID) solverID = reply.solverID
        return SolutionsSequence(solverID, reply.computationID, goal, channel)
    }

    override fun getFlags(): FlagStore {
        return FlagStore.of(solverFutureStub.getFlags(buildSolverId()).get().flagsList.parse())
    }

    override fun getStaticKB(): Theory {
        return getKBTheory { msg, obs -> solverStub.getStaticKB(msg, obs)}
    }

    override fun getDynamicKB(): Theory {
        return getKBTheory { msg, obs -> solverStub.getDynamicKB(msg, obs)}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getKBTheory(op: (SolverID, StreamObserver<ClauseMsg>) -> Unit): Theory {
        val future: CompletableDeferred<List<Clause>> = CompletableDeferred()
        op(buildSolverId(), object: StreamObserver<ClauseMsg> {
            val clauseList = mutableListOf<Clause>()
            override fun onNext(value: ClauseMsg) {
                clauseList.add(value.parseToClause())
            }
            override fun onError(t: Throwable) {
                future.completeExceptionally(t)
            }
            override fun onCompleted() { future.complete(clauseList) }
        })
        runBlocking {
            future.await()
        }
        return Theory.of(future.getCompleted())
    }

    override fun getLibraries(): List<String> {
        return solverFutureStub.getLibraries(buildSolverId()).get()
            .librariesList.map { it.name }
    }

    override fun getUnificator(): Unificator {
        return solverFutureStub.getUnificator(buildSolverId()).get().parse()
    }

    override fun getOperators(): OperatorSet {
        val operators = mutableListOf<Operator>()
        solverFutureStub.getOperators(buildSolverId()).get().operatorList.map {
            val operator = Operator.fromTerms(Integer.of(it.priority),
                Atom.of(it.specifier),
                Atom.of(it.functor))
            if(operator != null) operators.add(operator)
        }
        return OperatorSet(operators)
    }

    override fun getInputChannels(): Map<String, List<String>> {
        return solverFutureStub.getInputChannels(buildSolverId()).get()
            .channelList.map { Pair(it.name, it.contentList) }.toMap()
    }

    override fun getOutputChannels(): Map<String, List<String>> {
        return solverFutureStub.getOutputChannels(buildSolverId()).get()
            .channelList.map { Pair(it.name, it.contentList) }.toMap()
    }

    override fun writeOnInputChannel(channelID: String, vararg terms: String) {
        solverStub.writeOnInputChannel(InputChannelEvent.newBuilder().setSolverID(solverID).setChannelID(
            Channels.ChannelID.newBuilder().setName(channelID)
        ).addAllLine(terms.toList()).build(), object: StreamObserver<OperationResult> {
            override fun onNext(value: OperationResult?) {}
            override fun onError(t: Throwable?) {
                println(t)
            }
            override fun onCompleted() {}
        })
    }

    override fun readOnOutputChannel(channelID: String): String {
        return solverFutureStub.readFromOutputChannel(
            OutputChannelEvent.newBuilder()
            .setChannelID(Channels.ChannelID.newBuilder().setName(channelID))
            .setSolverID(solverID).build()).get().line
    }

    override fun readStreamOnOutputChannel(channelID: String): BlockingDeque<String> {
        val deque = LinkedBlockingDeque<String>()
        val stub = solverStub.readStreamFromOutputChannel(object: StreamObserver<ReadLine> {
            override fun onNext(value: ReadLine) {
                deque.putLast(value.line)
            }
            override fun onError(t: Throwable?) {
                println(t)
            }
            override fun onCompleted() {}
        })
        stub.onNext(OutputChannelEvent.newBuilder()
            .setChannelID(MessageBuilder.fromChannelIDToMsg(channelID))
            .setSolverID(solverID).build())
        openStreamObservers.add(stub)
        return deque
    }

    protected fun setOutChannel(type: String, op: (String)->Unit) {
        val stub = solverStub.readStreamFromOutputChannel(object: StreamObserver<ReadLine> {
            override fun onNext(value: ReadLine) {
                op(value.line)
            }
            override fun onError(t: Throwable?) { println(t) }
            override fun onCompleted() {}
        })
        stub.onNext(OutputChannelEvent.newBuilder()
            .setChannelID(MessageBuilder.fromChannelIDToMsg(type))
            .setSolverID(solverID).build())
        openStreamObservers.add(stub)
    }

    private fun buildRequestWithOptionsMessage(goal:Struct, options: SolveOptions): SolveRequest {
        val serializer = TermSerializer.of(MimeType.Json)
        val request = SolveRequest.newBuilder()
            .setSolverID(solverID).setStruct(serializer.serialize(goal))
            .addOptions(buildOption(TIMEOUT_OPTION, options.timeout))
            .addOptions(buildOption(LIMIT_OPTION, options.limit.toLong()))
        if(options.isEager) request.addOptions(buildOption(EAGER_OPTION))
        if(options.isLazy) request.addOptions(buildOption(LAZY_OPTION))
        //options.customOptions.forEach { request.addOptions(buildOption(it.key, (it.value))) }
        return request.build()
    }

    private fun buildOption(key: String, value: Long = -1): SolveRequest.Options {
        return SolveRequest.Options.newBuilder().setName(key).setValue(value).build()
    }

    private fun buildSolverId(): SolverID {
        return SolverID.newBuilder().setSolverID(this.solverID).build()
    }
}
