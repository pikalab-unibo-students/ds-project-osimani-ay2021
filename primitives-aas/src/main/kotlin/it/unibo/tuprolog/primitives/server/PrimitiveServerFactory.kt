package it.unibo.tuprolog.primitives.server

import io.grpc.ServerBuilder
import it.unibo.tuprolog.primitives.db.DbManager
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import java.util.concurrent.Executors

object PrimitiveServerFactory {
    fun startService(
        functor: String,
        arity: Int,
        primitive: DistribuitedPrimitive,
        port: Int = 8080,
        libraryName: String = ""
    ) {
        val executor = Executors.newCachedThreadPool()
        val service = PrimitiveServerWrapper.of(functor, arity, primitive, executor)
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