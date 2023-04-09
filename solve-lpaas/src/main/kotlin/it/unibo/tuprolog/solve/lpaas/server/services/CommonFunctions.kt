package it.unibo.tuprolog.solve.lpaas.server.services

import io.grpc.Status
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection

fun sendError(error: String, type: Status, responseObserver: StreamObserver<*>) {
    responseObserver.onError(
       type.withDescription(error)
            .asRuntimeException())
}

fun checkSolverExistence(solverID: String, responseObserver: StreamObserver<*>, operation: () -> Unit) {
    if(SolversCollection.contains(solverID)) operation()
    else
        sendError("The selected solver doesn't exist", Status.INVALID_ARGUMENT, responseObserver)
}

private const val STRING_LENGTH = 10
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun idGenerator(): String {
    return List(STRING_LENGTH) { charPool.random() }.joinToString("")
}