package it.unibo.tuprolog.solve.lpaas.client

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.*

interface ClientSolver {

    fun createSolver(staticKb: String = "", dynamicKb: String = "")

    fun getId(): String

    val replyPrinter: StreamObserver<SolutionReply>
    fun solveQuery(goal: String, callBack: StreamObserver<SolutionReply> = replyPrinter)

    fun solveQueryOnce(goal: String, callBack: StreamObserver<SolutionReply> = replyPrinter)

    fun solveQueryAsList(goal: String, callBack: StreamObserver<SolutionListReply>)

    fun solveWithTimeout(goal: String, timeout: Long = SolveOptions.MAX_TIMEOUT,
                         callBack: StreamObserver<SolutionReply> = replyPrinter)

    fun requestQueryWithOptions(goal: String,
                                timeout: Long = SolveOptions.MAX_TIMEOUT,
                                limit: Int = SolveOptions.ALL_SOLUTIONS,
                                laziness: Boolean = false,
                                eagerness: Boolean = true,
                                callback: StreamObserver<SolutionReply> = replyPrinter)
}

class ClientSolverImpl(staticKb: String = DEFAULT_STATIC_THEORY, dynamicKb: String = ""): ClientSolver {

    private var id = ""

    private val clientSolver: SolverGrpc.SolverStub =
        SolverGrpc.newStub(ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build())
    private val clientSolverFactory: SolverFactoryGrpc.SolverFactoryFutureStub =
        SolverFactoryGrpc.newFutureStub(ManagedChannelBuilder.forAddress("localhost", 8081)
            .usePlaintext()
            .build())

    init {
        createSolver(staticKb, dynamicKb)
    }

    override fun getId(): String = id

    override val replyPrinter = object : StreamObserver<SolutionReply> {
        override fun onNext(value: SolutionReply) { println(value.solvedQuery) }
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    override fun createSolver(staticKb: String, dynamicKb: String) {
        val createSolver: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(staticKb).setDynamicKb(dynamicKb).build()
        id = clientSolverFactory.produceSolver(createSolver).get().id
    }

    override fun solveQuery(goal: String, callBack: StreamObserver<SolutionReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(id).setStruct(goal).build()
        return clientSolver.solve(request, callBack)
    }

    override fun solveQueryOnce(goal: String, callBack: StreamObserver<SolutionReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(id).setStruct(goal).build()
        return clientSolver.solveOnce(request, callBack)
    }

    override fun solveQueryAsList(goal: String, callBack: StreamObserver<SolutionListReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(id).setStruct(goal).build()
        return clientSolver.solveList(request, callBack)
    }

    override fun solveWithTimeout(goal: String, timeout: Long, callBack: StreamObserver<SolutionReply>) {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(id).setTimeout(timeout).setStruct(goal).build()
        return clientSolver.solveWithTimeout(request, callBack)
    }

    private fun buildOption(name: String, value: Long = -1): Options {
        return Options.newBuilder().setName(name).setValue(value).build()
    }

    override fun requestQueryWithOptions(goal: String, timeout: Long, limit: Int, laziness: Boolean, eagerness: Boolean,
                                         callback: StreamObserver<SolutionReply>) {
        val request = SolveRequestWithOptions.newBuilder()
            .setId(id).setStruct(goal)
            .addOptions(buildOption(TIMEOUT_OPTION, timeout))
            .addOptions(buildOption(LIMIT_OPTION, limit.toLong()))
        if(eagerness) request.addOptions(buildOption(EAGER_OPTION))
        if(laziness) request.addOptions(buildOption(LAZY_OPTION))
        clientSolver.solveWithOptions(request.build(), callback)
    }
}