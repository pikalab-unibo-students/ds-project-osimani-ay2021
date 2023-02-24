package it.unibo.tuprolog.solve.lpaas.client

import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.*

interface ClientSolver {

    fun createSolver(staticKb: String = "", dynamicKb: String = "")
    fun solveQuery(goal: String, callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveWithTimeout(goal: String, timeout: Long = SolveOptions.MAX_TIMEOUT,
                         callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveQueryWithOptions(goal: String, options: SolveOptions,
                              callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveQueryOnce(goal: String, callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveQueryOnceWithTimeout(goal: String, timeout: Long, callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveQueryOnceWithOptions(goal: String, options: SolveOptions,
                                  callback: StreamObserver<SolutionReply> = replyPrinter)
    fun solveQueryAsList(goal: String, callback: StreamObserver<SolutionListReply> = replyListPrinter)
    fun solveQueryAsListWithTimeout(goal: String, timeout: Long, callback: StreamObserver<SolutionListReply> = replyListPrinter)
    fun solveQueryAsListWithOptions(goal: String, options: SolveOptions,
                                    callback: StreamObserver<SolutionListReply> = replyListPrinter)
    fun getNextSolution(callback: StreamObserver<SolutionReply> = replyPrinter)
}

private val replyPrinter = object : StreamObserver<SolutionReply> {
    override fun onNext(value: SolutionReply) { println(value) }
    override fun onError(t: Throwable) { t.printStackTrace() }
    override fun onCompleted() {}
}

private val replyListPrinter = object : StreamObserver<SolutionListReply> {
    override fun onNext(value: SolutionListReply) { value.solutionList.forEach { println(it) }}
    override fun onError(t: Throwable) { t.printStackTrace() }
    override fun onCompleted() {}
}

class ClientSolverImpl(staticKb: String = DEFAULT_STATIC_THEORY, dynamicKb: String = ""): ClientSolver {

    private var solverId = ""
    private var currentQuery = ""

    private val clientSolver: SolverGrpc.SolverFutureStub =
        SolverGrpc.newFutureStub(ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build())
    private val clientGetSolution: SolverGrpc.SolverStub =
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

    override fun createSolver(staticKb: String, dynamicKb: String) {
        val createSolver: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(staticKb).setDynamicKb(dynamicKb).build()
        solverId = clientSolverFactory.produceSolver(createSolver).get().id
    }

    override fun solveQuery(goal: String, callback: StreamObserver<SolutionReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(solverId).setStruct(goal).build()
        requestNext(clientSolver.solve(request), callback)
    }

    override fun solveWithTimeout(goal: String, timeout: Long, callback: StreamObserver<SolutionReply>) {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(solverId).setTimeout(timeout).setStruct(goal).build()
        requestNext(clientSolver.solveWithTimeout(request), callback)
    }

    override fun solveQueryWithOptions(goal: String, options: SolveOptions,
                                       callback: StreamObserver<SolutionReply>) {
        requestNext(clientSolver.solveWithOptions(buildRequestWithOptionsMessage(goal, options)), callback)
    }

    override fun solveQueryOnce(goal: String, callback: StreamObserver<SolutionReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(solverId).setStruct(goal).build()
        requestNext(clientSolver.solveOnce(request), callback)
    }

    override fun solveQueryOnceWithTimeout(goal: String, timeout: Long, callback: StreamObserver<SolutionReply>) {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(solverId).setTimeout(timeout).setStruct(goal).build()
        requestNext(clientSolver.solveOnceWithTimeout(request), callback)
    }

    override fun solveQueryOnceWithOptions(goal: String, options: SolveOptions,
                                           callback: StreamObserver<SolutionReply>) {
        requestNext(clientSolver.solveOnceWithOptions(buildRequestWithOptionsMessage(goal, options)), callback)
    }

    override fun solveQueryAsList(goal: String, callback: StreamObserver<SolutionListReply>) {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(solverId).setStruct(goal).build()
        clientGetSolution.solveList(request, callback)
    }

    override fun solveQueryAsListWithTimeout(goal: String, timeout: Long, callback: StreamObserver<SolutionListReply>) {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(solverId).setTimeout(timeout).setStruct(goal).build()
        clientGetSolution.solveListWithTimeout(request, callback)
    }

    override fun solveQueryAsListWithOptions(goal: String, options: SolveOptions,
                                             callback: StreamObserver<SolutionListReply>) {
        clientGetSolution.solveListWithOptions(buildRequestWithOptionsMessage(goal, options), callback)
    }

    private fun buildOption(name: String, value: Long = -1): Options {
        return Options.newBuilder().setName(name).setValue(value).build()
    }

    private fun buildRequestWithOptionsMessage(goal:String, options: SolveOptions): SolveRequestWithOptions {
        val request = SolveRequestWithOptions.newBuilder()
            .setId(solverId).setStruct(goal)
            .addOptions(buildOption(TIMEOUT_OPTION, options.timeout))
            .addOptions(buildOption(LIMIT_OPTION, options.limit.toLong()))
        if(options.isEager) request.addOptions(buildOption(EAGER_OPTION))
        if(options.isLazy) request.addOptions(buildOption(LAZY_OPTION))
        return request.build()
    }

    override fun getNextSolution(callback: StreamObserver<SolutionReply>) {
        val request = NextSolutionRequest.newBuilder()
            .setQuery(currentQuery).setId(solverId).build()
        clientGetSolution.nextSolution(request, callback)
    }

    private fun requestNext(stub: ListenableFuture<IteratorReply>, callback: StreamObserver<SolutionReply>) {
        currentQuery = stub.get().query
        getNextSolution(callback)
    }
}