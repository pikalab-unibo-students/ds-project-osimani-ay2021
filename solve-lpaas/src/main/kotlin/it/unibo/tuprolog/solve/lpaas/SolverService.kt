package it.unibo.tuprolog.solve.lpaas

import io.grpc.stub.StreamObserver

object SolverService : SolverGrpc.SolverImplBase() {

    private fun solve(struct: String): SolutionReply? {
        println(struct);
        return SolutionReply.newBuilder().setSolution("hello").build()
    }
    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        responseObserver.onNext(solve(request.struct))
        responseObserver.onCompleted()
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout?, responseObserver: StreamObserver<SolutionReply>?) {
        //super.solveWithTimeout(request, responseObserver)
    }

    override fun solveWithOption(request: SolveRequestWithOptions?, responseObserver: StreamObserver<SolutionReply>?) {
        //super.solveWithOption(request, responseObserver)
    }

    override fun solveList(request: SolveRequest?, responseObserver: StreamObserver<SolutionListReply>?) {
        //super.solveList(request, responseObserver)
    }

    override fun solveListWithTimeout(
        request: SolveRequestWithTimeout?,
        responseObserver: StreamObserver<SolutionListReply>?
    ) {
        //super.solveListWithTimeout(request, responseObserver)
    }

    override fun solveListWithOptions(
        request: SolveRequestWithOptions?,
        responseObserver: StreamObserver<SolutionListReply>?
    ) {
        //super.solveListWithOptions(request, responseObserver)
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout?,
        responseObserver: StreamObserver<SolutionReply>?
    ) {
        //super.solveOnceWithTimeout(request, responseObserver)
    }

    override fun solveOnce(request: SolveRequest?, responseObserver: StreamObserver<SolutionReply>?) {
        //super.solveOnce(request, responseObserver)
    }

    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions?,
        responseObserver: StreamObserver<SolutionReply>?
    ) {
        //super.solveOnceWithOptions(request, responseObserver)
    }


}