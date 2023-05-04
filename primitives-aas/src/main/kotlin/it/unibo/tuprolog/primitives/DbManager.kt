package it.unibo.tuprolog.primitives

import com.mongodb.ConnectionString
import it.unibo.tuprolog.primitives.impl.DbManagerImpl
import it.unibo.tuprolog.solve.Signature
import org.litote.kmongo.KMongo

interface DbManager {
    data class SerializedPrimitive(
        val functor: String,
        val arity: Int,
        val url: String,
        val port: Int,
        val libraryName: String = ""
    )

    fun addPrimitive(signature: Signature, url: String = "localhost",
                     port: Int = 8080, libraryName: String = "") =
        addPrimitive(signature.name, signature.arity, url, port, libraryName)

    fun addPrimitive(functor: String, arity: Int,
        url: String = "localhost", port: Int = 8080, libraryName: String = ""
    )

    fun getPrimitive(signature: Signature): Pair<String, Int>? = getPrimitive(signature.name, signature.arity)

    fun getPrimitive(functor: String, arity: Int): Pair<String, Int>?

    fun deletePrimitive(signature: Signature, libraryName: String) =
        deletePrimitive(signature.name, signature.arity, libraryName)

    fun deletePrimitive(functor: String, arity: Int, libraryName: String)

    fun getLibrary(libraryName: String): Set<Pair<String, Int>>

    companion object {
        private var manager: DbManager? = null

        private const val port = 27017
        private const val DB_USER="app_user"
        private val DB_PASS="app_password"
        //val URL_DOCKER = "mongodb://${DB_USER}:${DB_PASS}@mongodb"
        const val URL_LOCAL = "mongodb://localhost"
        //val URL_DOCKER_LOCAL = "mongodb://${DB_USER}:${DB_PASS}@0.0.0.0"

        init {
            try {
                manager = DbManagerImpl(URL_LOCAL, port)
            } catch (e: Exception) { println(e) }
        }

        fun get(): DbManager {
            if(manager != null)
                return manager!!
            else throw IllegalStateException("Must be initialized first")
        }

    }
}