package it.unibo.tuprolog.solve.lpaas.server.database

import com.mongodb.ConnectionString
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.deserializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serialize
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serializer
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import org.litote.kmongo.*

object DbManager {
    data class SerializedSolver(
        val id: String,
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
    private val solversDB: MongoCollection<SerializedSolver>
    private val channelsDB: MongoCollection<SerializedSolver>
    private const val port = 27017

    init {
        val client = KMongo.createClient(ConnectionString("mongodb://localhost:$port"))
        database = client.getDatabase("lpaas")
        solversDB = database.getCollection<SerializedSolver>()
        channelsDB = database.getCollection<SerializedSolver>()
    }

    /** Solve channels */
    fun addSolver(solverID: String, solver: Solver, mutable: Boolean = false) {
        solversDB.insertOne(SerializedSolver(
            solverID, solver.unificator.serialize(),
            solver.libraries.serialize(), solver.flags.serialize(),
            solver.staticKb.serialize(), solver.dynamicKb.serialize(),
            emptyMap(), emptyMap(), mutable)
        )
    }

    fun loadSolvers() {
        solversDB.find().forEach { doc ->
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
                id = doc.id
            )
        }
    }

    fun updateSolver(solverID: String) {
        val solver = SolversCollection.getSolver(solverID)
        solversDB.updateOne(SerializedSolver::id eq solverID,
            SetTo(SerializedSolver::unificator, solver.unificator.serialize()),
            SetTo(SerializedSolver::flags, solver.flags.serialize()),
            SetTo(SerializedSolver::runtime, solver.libraries.serialize()),
            SetTo(SerializedSolver::staticKB, solver.staticKb.serialize()),
            SetTo(SerializedSolver::dynamicKB, solver.dynamicKb.serialize()),
            SetTo(SerializedSolver::inputs, solver.inputChannels.keys),
            SetTo(SerializedSolver::outputs, solver.outputChannels.keys),
        )
    }
}