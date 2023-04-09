package it.unibo.tuprolog.solve.lpaas.server.database

import com.mongodb.ConnectionString
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.server.channels.ChannelObserver
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.deserializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serialize
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import org.litote.kmongo.*

class DbManager(uri: String) {
    data class SerializedSolver(
        val solverID: String,
        val unificator: Map<String, String>,
        val runtime: Set<String>,
        val flags: Map<String, String>,
        val staticKB: List<String>,
        val dynamicKB: List<String>,
        val inputs: Map<String, List<String>>,
        val outputs: Map<String, List<String>>,
        val mutable: Boolean
    )

    private val database: MongoDatabase
    private val solversCol: MongoCollection<SerializedSolver>

    init {
        val client = KMongo.createClient(ConnectionString(uri))
        database = client.getDatabase("lpaas")
        solversCol = database.getCollection<SerializedSolver>()
    }

    fun addSolver(solverID: String, mutable: Boolean = false) {
        val solver = SolversCollection.getSolver(solverID)
        val channels = SolversCollection.getChannelDequesOfSolver(solverID)
        val inputs = serializeChannels(channels.getInputChannels())
        val outputs = serializeChannels(channels.getOutputChannels())
        solversCol.insertOne(SerializedSolver(
            solverID, solver.unificator.serialize(),
            solver.libraries.serialize(), solver.flags.serialize(),
            solver.staticKb.serialize(), solver.dynamicKb.serialize(),
            inputs, outputs, mutable)
        )
    }

    fun loadSolvers() {
        solversCol.find().forEach { doc ->
            val scope = Scope.empty()
            val unificator = Unificator.strict(
                Substitution.of(doc.unificator.map {
                    Pair(scope.varOf(it.key), deserializer.deserialize(it.value)) }
                    .toMap()))
            val runtime = Runtime.of(doc.runtime.map { convertStringToKnownLibrary(it) })
            val flags = FlagStore.of(doc.flags.map { Pair(it.key, deserializer.deserialize(it.value)) }.toMap())
            val staticKB = Theory.of(doc.staticKB.map { deserializer.deserialize(it).castToClause() }.toList())
            val dynamicKB = Theory.of(doc.dynamicKB.map { deserializer.deserialize(it).castToClause() }.toList())
            SolversCollection.addSolver(
                unificator = unificator,
                runtime = runtime,
                flagStore = flags,
                staticKb = staticKB,
                dynamicKb = dynamicKB,
                inputs = doc.inputs,
                outputs = doc.outputs,
                defaultBuiltIns = false,
                mutable = doc.mutable,
                id = doc.solverID
            )
        }
    }

    fun updateSolver(solverID: String) {
        val solver = SolversCollection.getSolver(solverID)
        val channels = SolversCollection.getChannelDequesOfSolver(solverID)
        val inputs = serializeChannels(channels.getInputChannels())
        val outputs = serializeChannels(channels.getOutputChannels())
        solversCol.updateOne(SerializedSolver::solverID eq solverID,
            SetTo(SerializedSolver::unificator, solver.unificator.serialize()),
            SetTo(SerializedSolver::flags, solver.flags.serialize()),
            SetTo(SerializedSolver::runtime, solver.libraries.serialize()),
            SetTo(SerializedSolver::staticKB, solver.staticKb.serialize()),
            SetTo(SerializedSolver::dynamicKB, solver.dynamicKb.serialize()),
            SetTo(SerializedSolver::inputs, inputs),
            SetTo(SerializedSolver::outputs, outputs),
        )
    }

    fun deleteSolver(solverID: String) {
        solversCol.deleteOne(SerializedSolver::solverID eq solverID)
    }

    fun deleteAll() {
        solversCol.drop()
    }

    private fun <T : Any> serializeChannels(channels: Map<String, ChannelObserver<T>>): Map<String, List<T>> {
        return channels.map { Pair(it.key, it.value.getCurrentContent()) }.toMap()
    }

    companion object {
        var manager: DbManager? = null

        private val port = 27017
        private val DB_USER="app_user"
        private val DB_PASS="app_password"
        val URL_DOCKER = "mongodb://${DB_USER}:${DB_PASS}@mongodb"
        val URL_LOCAL = "mongodb://localhost"
        val URL_DOCKER_LOCAL = "mongodb://${DB_USER}:${DB_PASS}@0.0.0.0"

        fun init(url: String, port: Int = this.port) {
            manager = DbManager("$url:$port")
        }

        fun get(): DbManager {
            if(manager != null)
                return manager!!
            else throw IllegalStateException("Must be initialized first")
        }

    }
}