package it.unibo.tuprolog.solve.lpaas.server.services

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.lpaas.MutableSolverGrpc
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.*
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperationResult
import it.unibo.tuprolog.solve.lpaas.solveMessage.SolverID
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.solve.lpaas.util.parsers.deserializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.fromClauseToMsg
import it.unibo.tuprolog.solve.lpaas.util.parsers.fromTheoryToMsg
import it.unibo.tuprolog.theory.RetractResult
import it.unibo.tuprolog.theory.Theory

object MutableSolverService: MutableSolverGrpc.MutableSolverImplBase() {

    private val solvers = SolversCollection

    override fun appendDynamicKB(responseObserver: StreamObserver<OperationResult>): StreamObserver<MutableClause> {
        return doOperationWithKB(responseObserver) {solver, theory -> solver.appendDynamicKb(theory)}
    }

    override fun loadDynamicKB(responseObserver: StreamObserver<OperationResult>): StreamObserver<MutableClause> {
        return doOperationWithKB(responseObserver) {solver, theory -> solver.loadDynamicKb(theory)}
    }

    override fun loadStaticKB(responseObserver: StreamObserver<OperationResult>): StreamObserver<MutableClause> {
        return doOperationWithKB(responseObserver) {solver, theory -> solver.loadStaticKb(theory)}
    }

    override fun appendStaticKB(responseObserver: StreamObserver<OperationResult>): StreamObserver<MutableClause> {
        return doOperationWithKB(responseObserver) {solver, theory -> solver.appendStaticKb(theory)}
    }

    private fun doOperationWithKB(responseObserver: StreamObserver<OperationResult>, op: (MutableSolver, Theory) -> Unit): StreamObserver<MutableClause> {
        return object: StreamObserver<MutableClause> {
            var solverID = ""
            val clauseList = mutableListOf<Clause>()
            override fun onNext(value: MutableClause) {
                if(!value.clause.content.isEmpty()) clauseList.add(deserializer.deserialize(value.clause.content).castToClause())
                if(solverID.isEmpty()) solverID = value.solverID
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {
                val solver = solvers.getMutableSolver(solverID)
                if(solver != null ) {
                    op(solver, Theory.of(clauseList))
                    responseObserver.onNext(buildOperationResult())
                } else {
                    responseObserver.onNext(buildOperationResult("The selected solver is not mutable"))
                }
                responseObserver.onCompleted()
            }
        }
    }

    override fun loadLibrary(request: MutableLibrary, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.loadLibrary(convertStringToKnownLibrary(request.library.name))},
            responseObserver)
    }

    override fun assertA(request: MutableClause, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.assertA(deserializer.deserialize(request.clause.content).castToStruct())},
            responseObserver)
    }

    override fun assertZ(request: MutableClause, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.assertZ(deserializer.deserialize(request.clause.content).castToStruct())},
            responseObserver)
    }

    override fun resetDynamicKb(request: SolverID, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.resetDynamicKb() },
            responseObserver)
    }

    override fun resetStaticKb(request: SolverID, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.resetStaticKb() },
            responseObserver)
    }

    override fun retract(request: MutableClause, responseObserver: StreamObserver<RetractResultMsg>) {
        genericRetract(request.solverID, request.clause.content, responseObserver) {
            solver, struct -> solver.retract(struct)
        }
    }

    override fun retractAll(request: MutableClause, responseObserver: StreamObserver<RetractResultMsg>) {
        genericRetract(request.solverID, request.clause.content, responseObserver) {
                solver, struct -> solver.retractAll(struct)
        }
    }

    private fun genericRetract(solverID: String, structToRetract: String,
                               responseObserver: StreamObserver<RetractResultMsg>,
                               operation: (MutableSolver, Struct) -> RetractResult<Theory>) {
        try {
            val result = operation(
                solvers.getMutableSolver(solverID)!!,
                deserializer.deserialize(structToRetract).castToStruct())
            val responseBuilder = RetractResultMsg.newBuilder().setTheory(fromTheoryToMsg(result.theory))
                .addAllClauses(result.clauses?.map { fromClauseToMsg(it) })
            if(result.isSuccess) {
                responseObserver.onNext(responseBuilder.setIsSuccess(true).build())
            } else {
                responseObserver.onNext(responseBuilder.setIsFailure(true).build())
            }
        } catch (e: Exception) {
            println(e)
        }
        responseObserver.onCompleted()
    }

    override fun setFlag(request: MutableFlag, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.setFlag( Pair(request.flag.name, deserializer.deserialize(request.flag.value)))},
            responseObserver)
    }

    override fun setLibraries(request: MutableRuntime, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.setRuntime(Runtime.of(
                request.runtime.librariesList.map { lib -> convertStringToKnownLibrary(lib.name)  })
            )},
            responseObserver)
    }

    override fun unloadLibrary(request: MutableLibrary, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.unloadLibrary(convertStringToKnownLibrary(request.library.name)) },
            responseObserver)
    }

    override fun setChannel(request: MutableChannelID, responseObserver: StreamObserver<OperationResult>) {
        val collection = SolversCollection.getChannelDequesOfSolver(request.solverID)
        doOperationOnMutableSolver(request.solverID,
            { when(request.type) {
                (MutableChannelID.CHANNEL_TYPE.INPUT) -> {
                    val channel = collection.addInputChannel(InputStore.STDIN, request.channel.content)
                    it.setStandardInput(channel)
                }
                else -> {}
            }}, responseObserver)
    }

    private fun doOperationOnMutableSolver(solverID: String, operation: (MutableSolver) -> Unit, responseObserver: StreamObserver<OperationResult>) {
        try {
            operation(solvers.getMutableSolver(solverID)!!)
            responseObserver.onNext(buildOperationResult())
        } catch (e: Exception) {
            responseObserver.onNext(buildOperationResult("The selected solver is not mutable"))
        }
        responseObserver.onCompleted()
    }

    private fun buildOperationResult(error: String = ""): OperationResult {
        return if(error.isEmpty()) OperationResult.newBuilder().setResult(true).build() else
            OperationResult.newBuilder().setResult(false).setError(error).build()
    }
}