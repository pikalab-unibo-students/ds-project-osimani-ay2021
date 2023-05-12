package it.unibo.tuprolog.primitives.server

import io.grpc.ServerBuilder
import it.unibo.tuprolog.primitives.db.DbManager
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitiveWrapper
import java.util.concurrent.Executors

object PrimitiveServerFactory {
    fun startService(
        name: String,
        arity: Int,
        primitive: DistribuitedPrimitive,
        port: Int = 8080,
        libraryName: String = ""
    ) {
        val executor = Executors.newCachedThreadPool()
        val service = PrimitiveServerWrapper.of(name, arity, primitive, executor)
        val genericPrimitive = ServerBuilder.forPort(port)
            .addService(service)
            .executor(executor)
            .build()
        genericPrimitive!!.start()
        DbManager.get().addPrimitive(service.signature, port = port, libraryName =  libraryName)
        Runtime.getRuntime().addShutdownHook(Thread {
            DbManager.get().deletePrimitive(service.signature, libraryName)
            genericPrimitive.shutdownNow()
        })
        println("${service.signature.name} listening on port $port")
        genericPrimitive.awaitTermination()
    }

    fun startService(
        primitive: DistributedPrimitiveWrapper,
        port: Int = 8080,
        libraryName: String = ""
    ) {
        val executor = Executors.newCachedThreadPool()
        val service = PrimitiveServerWrapper.of(primitive, executor)
        val genericPrimitive = ServerBuilder.forPort(port)
            .addService(service)
            .executor(executor)
            .build()
        genericPrimitive!!.start()
        DbManager.get().addPrimitive(service.signature, port = port, libraryName =  libraryName)
        Runtime.getRuntime().addShutdownHook(Thread {
            DbManager.get().deletePrimitive(service.signature, libraryName)
            genericPrimitive.shutdownNow()
        })
        println("${service.signature.name} listening on port $port")
        genericPrimitive.awaitTermination()
    }
}