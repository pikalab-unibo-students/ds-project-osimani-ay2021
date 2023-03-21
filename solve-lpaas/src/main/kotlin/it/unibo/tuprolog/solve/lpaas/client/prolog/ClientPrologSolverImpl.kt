package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.serialize.MimeType
import it.unibo.tuprolog.serialize.TermSerializer
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg.ClauseMsg
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*
import it.unibo.tuprolog.solve.lpaas.util.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.*
import it.unibo.tuprolog.solve.lpaas.util.parsers.fromTheoryToMsg
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

open class ClientPrologSolverImpl(unificator: Unificator, libraries: Set<String>,
                                  flags: FlagStore, staticKb: Theory, dynamicKb: Theory,
                                  inputChannels: Map<String, String>, outputChannels: Set<String>,
                                  defaultBuiltins: Boolean):
    ClientSolver {

    protected val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    protected val solverStub: SolverGrpc.SolverStub = SolverGrpc.newStub(channel)
    private val solverFutureStub: SolverGrpc.SolverFutureStub = SolverGrpc.newFutureStub(channel)

    protected var solverID: String = generateSolverID(unificator, libraries, flags, staticKb,
        dynamicKb, inputChannels, outputChannels, defaultBuiltins)

    protected open fun generateSolverID(unificator: Unificator, libraries: Set<String>,
                                        flags: FlagStore, staticKb: Theory, dynamicKb: Theory,
                                        inputChannels: Map<String, String>, outputChannels: Set<String>,
                                        defaultBuiltins: Boolean): String {
        val createSolverRequest: SolverRequest = SolverRequest.newBuilder()
            .setUnificator(fromUnificatorToMsg(unificator))
            .setRuntime(fromLibrariesToMsg(libraries))
            .setFlags(fromFlagsToMsg(flags))
            .setStaticKb(fromTheoryToMsg(staticKb))
            .setDynamicKb(fromTheoryToMsg(dynamicKb))
            .setInputStore(fromChannelsToMsg(inputChannels))
            .setOutputStore(fromChannelsToMsg(outputChannels))
            .setDefaultBuiltIns(defaultBuiltins).build()
        return SolverFactoryGrpc.newFutureStub(channel).solverOf(createSolverRequest).get().id
    }

    override fun closeClient() {
        if(!channel.isTerminated) {
            openStreamObservers.forEach { it.onCompleted() }
            channel.shutdown()
            channel.awaitTermination(1, TimeUnit.SECONDS)
        }
    }

    override fun solve(goal: Struct, options: SolveOptions): SolutionsSequence {
        val reply = solverFutureStub.solve(buildRequestWithOptionsMessage(goal, options)).get()
        if(solverID != reply.solverID) solverID = reply.solverID
        return SolutionsSequence(solverID, reply.computationID, goal, channel)
    }

    override fun getFlags(): FlagStore {
        val flagsMap = mutableMapOf<String, Term>()
        solverFutureStub.getFlags(buildSolverId()).get().flagsList.forEach {
            flagsMap[it.name] = deserializer.deserialize(it.value)
        }
        return FlagStore.of(flagsMap)
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
                clauseList.add(deserializer.deserialize(value.content).castToClause())
            }
            override fun onError(t: Throwable?) {}
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
        val substitution = mutableMapOf<Var, Term>()
        solverFutureStub.getUnificator(buildSolverId()).get().substitutionList.map {
            substitution[deserializer.deserialize(it.`var`).castToVar()] =
                deserializer.deserialize((it.term))
        }
        return Unificator.naive(Substitution.of(substitution))
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

    override fun getInputChannels(): List<String> {
        return solverFutureStub.getInputChannels(buildSolverId()).get()
            .channelList.map { it.name }
    }

    override fun getOutputChannels(): List<String> {
        return solverFutureStub.getOutputChannels(buildSolverId()).get()
            .channelList.map { it.name }
    }

    protected val openStreamObservers: MutableList<StreamObserver<*>> = mutableListOf()

    override fun writeOnInputChannel(channelID: String): StreamObserver<String> {
        val stub = solverStub.writeOnInputChannel(object: StreamObserver<OperationResult> {
            override fun onNext(value: OperationResult) {}
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        })
        openStreamObservers.add(stub)
        return object: StreamObserver<String> {
            override fun onNext(value: String) {
                stub.onNext(
                    LineEvent.newBuilder().setSolverID(solverID).setChannelID(
                        Channels.ChannelID.newBuilder().setName(channelID)
                    ).setLine(value).build()
                )
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() { stub.onCompleted() }
        }
    }

    override fun readOnOutputChannel(channelID: String): String {
        return solverFutureStub.readFromOutputChannel(
            OutputChannelEvent.newBuilder()
            .setChannelID(Channels.ChannelID.newBuilder().setName(channelID))
            .setSolverID(solverID).build()).get().line
    }

    override fun readStreamOnOutputChannel(channelID: String): BlockingDeque<String> {
        val deque = LinkedBlockingDeque<String>()
        val stub = solverStub.readStreamFromOutputChannel(object: StreamObserver<LineEvent> {
            override fun onNext(value: LineEvent) {
                deque.putLast(value.line)
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        })
        stub.onNext(OutputChannelEvent.newBuilder()
            .setChannelID(fromChannelIDToMsg(channelID))
            .setSolverID(solverID).build())
        openStreamObservers.add(stub)
        return deque
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