import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolveRequest
import it.unibo.tuprolog.solve.lpaas.SolveRequestWithTimeout
import it.unibo.tuprolog.solve.lpaas.SolverFactoryGrpc
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import it.unibo.tuprolog.solve.lpaas.SolverReply
import it.unibo.tuprolog.solve.lpaas.SolverRequest
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolverImpl
import it.unibo.tuprolog.solve.lpaas.server.SolverService
import java.util.concurrent.TimeUnit

fun main(/*args: Array<String>*/) {
    val channelFactory = ManagedChannelBuilder.forAddress("localhost", 8081)
        .usePlaintext()
        .build()

    val channelQuery = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    val client: ClientSolver = ClientSolverImpl(channelFactory, channelQuery)
    val client2: ClientSolver = ClientSolverImpl(channelFactory, channelQuery)
    client.createSolver("""
                p(a).
                p(c).
                """.trimIndent())
    channelFactory.awaitTermination(2, TimeUnit.SECONDS)
    client2.requestQuery("f(Y)")
    client.requestQuery("p(X)")




    channelQuery.awaitTermination(5, TimeUnit.SECONDS)
    channelQuery.shutdown()
}