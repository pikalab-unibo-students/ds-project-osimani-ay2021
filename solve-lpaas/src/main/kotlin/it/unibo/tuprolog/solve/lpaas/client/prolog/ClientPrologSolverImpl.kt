package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.util.*
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg.ClauseMsg
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import kotlinx.coroutines.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit

internal class ClientPrologSolverImpl(staticKb: Theory, dynamicKb: Theory):
    ClientSolver {

    private var solverID: String

    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    private val solverFutureStub: SolverGrpc.SolverFutureStub = SolverGrpc.newFutureStub(channel)

    init {
        val createSolverRequest: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(fromTheoryToMsg(staticKb))
            .setDynamicKb(fromTheoryToMsg(dynamicKb)).build()
        solverID = SolverFactoryGrpc.newFutureStub(channel).solverOf(createSolverRequest).get().id
    }

    private fun fromTheoryToMsg(theory: Theory): TheoryMsg {
        val builder = TheoryMsg.newBuilder()
        theory.forEach { builder.addClause(TheoryMsg.ClauseMsg.newBuilder().setContent(it.toString())) }
        return builder.build()
    }

    override fun closeClient() {
        channel.shutdown()
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

    override fun solve(goal: String): SolutionsSequence {
        return solve(goal, SolveOptions.DEFAULT)
    }

    override fun solve(goal: String, timeout: TimeDuration): SolutionsSequence {
        return solve(goal, SolveOptions.allLazilyWithTimeout(timeout))
    }

    override fun solve(goal: String, options: SolveOptions): SolutionsSequence {
        val reply = solverFutureStub.solve(buildRequestWithOptionsMessage(goal, options)).get()
        solverID = reply.solverID
        return SolutionsSequence(solverID, reply.computationID, reply.query, channel)
    }

    override fun solveList(goal: String): List<Solution> {
        return solve(goal).asSequence().toList()
    }

    override fun solveList(goal: String, timeout: TimeDuration): List<Solution> {
        return solve(goal, timeout).asSequence().toList()
    }

    override fun solveList(goal: String, options: SolveOptions): List<Solution> {
        return solve(goal, options).asSequence().toList()
    }

    override fun solveOnce(goal: String): Solution {
        return solve(goal, SolveOptions.someLazily(1)).getSolution(0)
    }

    override fun solveOnce(goal: String, timeout: TimeDuration): Solution {
        return solve(goal, SolveOptions.someLazilyWithTimeout(1, timeout)).getSolution(0)
    }

    override fun solveOnce(goal: String, options: SolveOptions): Solution {
        return solve(goal, options.setLimit(1)).getSolution(0)
    }

    override fun getFlags(): FlagStore {
        val flagsMap = mutableMapOf<String, Term>()
        solverFutureStub.getFlags(buildSolverId()).get().flagsList.forEach {
            flagsMap[it.name] = Term.parse(it.value)
        }
        return FlagStore.of(flagsMap)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStaticKB(): Theory {
        val future: CompletableDeferred<List<Clause>> = CompletableDeferred()
        solverStub.getStaticKB(buildSolverId(), generateStreamObserverOfTheory(future))
        runBlocking {
            future.await()
        }
        return Theory.of(future.getCompleted())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDynamicKB(): Theory {
        val future: CompletableDeferred<List<Clause>> = CompletableDeferred()
        solverStub.getDynamicKB(buildSolverId(), generateStreamObserverOfTheory(future))
        runBlocking {
            future.await()
        }
        return Theory.of(future.getCompleted())
    }

    private val solverStub: SolverGrpc.SolverStub = SolverGrpc.newStub(channel)

    private fun generateStreamObserverOfTheory(future: CompletableDeferred<List<Clause>>): StreamObserver<ClauseMsg> {
        return object: StreamObserver<ClauseMsg> {
            val clauseList = mutableListOf<Clause>()
            override fun onNext(value: ClauseMsg) {
                clauseList.add(Clause.parse(value.content))
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() { future.complete(clauseList) }
        }
    }

    override fun getLibraries(): List<String> {
        return solverFutureStub.getLibraries(buildSolverId()).get()
            .librariesList.map { it.name }
    }

    override fun getUnificator(): Unificator {
        val substitution = mutableMapOf<Var, Term>()
        solverFutureStub.getUnificator(buildSolverId()).get().substitutionList.map {
            substitution[Var.of(it.`var`)] = Term.parse(it.term)
        }
        return Unificator.naive(Substitution.of(substitution))
    }

    override fun getOperators(): OperatorSet {
        val operators = mutableListOf<Operator>()
        solverFutureStub.getOperators(buildSolverId()).get().operatorList.map {
            val operator = Operator.fromTerms(Integer.Companion.of(it.priority), Atom.of(it.specifier), Atom.of(it.functor))
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

    /** To FIX **/
    override fun writeOnInputChannel(channelID: String, message: String): BlockingDeque<String> {
        solverStub.writeOnInputChannel(object: StreamObserver<LineEvent> {
            override fun onNext(value: LineEvent?) {}
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        })
        return LinkedBlockingDeque()
    }

    /** To FIX **/
    override fun readOnOutputChannel(channelID: String, callback: (String) -> Unit) {
        solverStub.readFromOutputChannel(
            OutputChannelEvent.newBuilder()
            .setChannelID(Channels.ChannelID.newBuilder().setName(channelID))
            .setSolverID(solverID).build(),
            object: StreamObserver<LineEvent> {
                override fun onNext(value: LineEvent) { callback(value.line) }
                override fun onError(t: Throwable?) {}
                override fun onCompleted() { }
        })
    }

    private fun buildRequestWithOptionsMessage(goal:String, options: SolveOptions): SolveRequest {
        val request = SolveRequest.newBuilder()
            .setSolverID(solverID).setStruct(goal)
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