package it.unibo.tuprolog.primitives.server

import io.grpc.ServerBuilder
import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.solve.library.Library

object PrimitiveServerFactory {
    fun startService(service: PrimitiveServerWrapper, port: Int = 8080, libraryName: String = "") {
        val genericPrimitive = ServerBuilder.forPort(port)
            .addService(service)
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

    fun startLibraryServers(library: Library, initialPort: Int = 8080) {
        val list = library.primitives.map {
            PrimitiveServerWrapper.of(it.key.name, it.key.arity) { request, _ ->
                    it.value.solve(request)
                }
            }
        var port = initialPort
        list.forEach {
            startService(it, port++, library.alias)
        }
    }

}