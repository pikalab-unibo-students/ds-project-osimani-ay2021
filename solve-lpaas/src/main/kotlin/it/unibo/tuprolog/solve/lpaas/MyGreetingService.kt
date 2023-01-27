package it.unibo.tuprolog.solve.lpaas

import io.grpc.stub.StreamObserver

object MyGreetingService : GreeterGrpc.GreeterImplBase() {
    override fun sayHello(request: HelloRequest?, responseObserver: StreamObserver<HelloReply>?) {
        TODO()
    }

    override fun sayHelloToBunch(request: ArrayOfHelloRequests?, responseObserver: StreamObserver<HelloReply>?) {
        TODO()
    }

    override fun sayHelloToBunchAsStream(
        request: ArrayOfHelloRequests?,
        responseObserver: StreamObserver<HelloReply>?
    ) {
        TODO()
    }

    override fun sayHelloToMany(responseObserver: StreamObserver<HelloReply>?): StreamObserver<HelloRequest> {
        TODO()
    }

    override fun sayHelloToManyAsStream(responseObserver: StreamObserver<HelloReply>?): StreamObserver<HelloRequest> {
        TODO()
    }
}