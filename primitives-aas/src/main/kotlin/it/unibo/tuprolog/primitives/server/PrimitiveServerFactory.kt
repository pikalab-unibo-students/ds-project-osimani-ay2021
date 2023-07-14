package it.unibo.tuprolog.primitives.server

import io.grpc.Server
import io.grpc.ServerBuilder
import it.unibo.tuprolog.primitives.db.DbManager
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import java.util.concurrent.Executors

object PrimitiveServerFactory {
    fun startService(
        name: String,
        arity: Int,
        primitive: DistributedPrimitive,
        port: Int = 8080,
        libraryName: String = ""
    ) {
        startService(DistributedPrimitiveWrapper(name, arity, primitive), port, libraryName)
    }

    fun startService(
        primitive: DistributedPrimitiveWrapper,
        port: Int = 8080,
        libraryName: String = ""
    ): Server {
        val executor = Executors.newCachedThreadPool()
        val service = PrimitiveServerWrapper.of(primitive, executor)
        val genericPrimitive = ServerBuilder.forPort(port)
            .addService(service)
            .executor(executor)
            .build()
        genericPrimitive!!.start()
        //DbManager.get().addPrimitive(service.signature, port = port, libraryName =  libraryName)
        Runtime.getRuntime().addShutdownHook(Thread {
            //DbManager.get().deletePrimitive(service.signature, libraryName)
            genericPrimitive.shutdownNow()
        })
        println("${service.signature.name} of $libraryName listening on port $port")
        return genericPrimitive
    }
}