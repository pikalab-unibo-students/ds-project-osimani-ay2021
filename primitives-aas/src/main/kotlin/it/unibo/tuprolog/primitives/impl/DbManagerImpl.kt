package it.unibo.tuprolog.primitives.impl

import com.mongodb.ConnectionString
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.solve.Signature
import org.litote.kmongo.*

class DbManagerImpl(url: String, port: Int): DbManager {
    data class SerializedPrimitive(
        val functor: String,
        val arity: Int,
        val url: String,
        val port: Int,
        val libraryName: String = ""
    )

    private val primitivesDB: MongoCollection<SerializedPrimitive>

    init {
        val db = KMongo.createClient(ConnectionString("${url}:${port}"))
            .getDatabase("primitives")
        primitivesDB = db.getCollection<SerializedPrimitive>()
    }

    override fun addPrimitive(functor: String, arity: Int,
                              url: String, port: Int, libraryName: String) {
        checkInitialization {
            if(getPrimitive(functor, arity) == null)
                primitivesDB.insertOne(SerializedPrimitive(functor, arity, url, port, libraryName))
            else
                //To choose between error and update
                primitivesDB.updateOne(
                    Filters.and(
                        SerializedPrimitive::functor eq functor,
                        SerializedPrimitive::arity eq arity,
                        SerializedPrimitive::libraryName eq libraryName),
                    SetTo(SerializedPrimitive::url, url),
                    SetTo(SerializedPrimitive::port, port))
        }
    }

    override fun getPrimitive(functor: String, arity: Int): Pair<String, Int>? {
        val result =
            primitivesDB.find(SerializedPrimitive::functor eq functor, SerializedPrimitive::arity eq arity).first()
        if (result != null) {
            return Pair(result.url, result.port)
        } else {
            return null
        }
    }

    override fun deletePrimitive(functor: String, arity: Int, libraryName: String) {
        primitivesDB
            .deleteOne(
                SerializedPrimitive::functor eq functor,
                SerializedPrimitive::arity eq arity,
                SerializedPrimitive::libraryName eq libraryName)
    }

    override fun getLibrary(libraryName: String): Set<Pair<String, Int>> {
        val result =
            primitivesDB.find(SerializedPrimitive::libraryName eq libraryName)
        return result.map {
            Pair(it.functor, it.arity)
        }.toSet()
    }

    private fun checkInitialization(op: () -> Unit) {
        try {
            op()
        } catch(_:Exception) { println("The database is not connected") }
    }
}