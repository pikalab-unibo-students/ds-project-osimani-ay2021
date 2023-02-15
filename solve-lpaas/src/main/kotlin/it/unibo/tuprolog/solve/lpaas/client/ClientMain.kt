package it.unibo.tuprolog.solve.lpaas.client

import io.grpc.Channel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.EAGER_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LAZY_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LIMIT_OPTION
import it.unibo.tuprolog.solve.lpaas.util.TIMEOUT_OPTION

interface ClientSolver {

    fun createSolver(staticKb: String = "", dynamicKb: String = "")

    fun requestQuery(goal: String)

    fun requestQueryWithOptions(goal: String,
                                timeout: Long = SolveOptions.MAX_TIMEOUT,
                                limit: Int = SolveOptions.ALL_SOLUTIONS,
                                laziness: Boolean = false,
                                eagerness: Boolean = true)
}

class ClientSolverImpl(private val channelFactory: Channel,private val channelQuery: Channel): ClientSolver {

    private var id = ""

    private val replyIdGetter: StreamObserver<SolverReply> = object : StreamObserver<SolverReply> {
        override fun onNext(value: SolverReply) {
            id = value.id
        }
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    private val replyPrinter: StreamObserver<SolutionReply> = object : StreamObserver<SolutionReply> {
        override fun onNext(value: SolutionReply) { println(value.solution) }
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    override fun createSolver(staticKb: String, dynamicKb: String) {
        val createSolver: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(staticKb).setDynamicKb(dynamicKb).build()
        val clientSolverFactory = SolverFactoryGrpc.newStub(channelFactory)
        clientSolverFactory.produceSolver(createSolver, replyIdGetter)
    }

    override fun requestQuery(goal: String) {

        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(id).setStruct(goal).build()
        val clientSolverFactory = SolverGrpc.newStub(channelQuery)
        clientSolverFactory.solve(request, replyPrinter)
    }

    private fun buildOption(name: String, value: Long = -1): Options {
        return Options.newBuilder().setName(name).setValue(value).build()
    }

    override fun requestQueryWithOptions(goal: String, timeout: Long, limit: Int, laziness: Boolean, eagerness: Boolean) {
        val request = SolveRequestWithOptions.newBuilder()
            .setId(id).setStruct(goal)
            .setOptions(0, buildOption(TIMEOUT_OPTION, timeout))
            .setOptions(1, buildOption(LIMIT_OPTION, limit.toLong()))
        if(eagerness) request.setOptions(2, buildOption(EAGER_OPTION))
        if(laziness) request.setOptions(3, buildOption(LAZY_OPTION))
        val clientSolverFactory = SolverGrpc.newStub(channelQuery)
        clientSolverFactory.solveWithOptions(request.build(), replyPrinter)
    }
}