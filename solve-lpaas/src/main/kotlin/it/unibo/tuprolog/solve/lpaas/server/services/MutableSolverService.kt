package it.unibo.tuprolog.solve.lpaas.server.services

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.lpaas.MutableSolverGrpc
import it.unibo.tuprolog.solve.lpaas.mutableSolverMessages.*
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.server.database.DbManager
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperationResult
import it.unibo.tuprolog.solve.lpaas.solveMessage.SolverID
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parse
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parseToClause
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parseToStruct
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toMsg
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
                checkSolverExistence(value.solverID, responseObserver) {
                    if (value.clause.content.isNotEmpty()) clauseList.add(value.clause.parseToClause())
                    if (solverID.isEmpty()) solverID = value.solverID
                }
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {
                if(solvers.contains(solverID)) {
                    val solver = solvers.getMutableSolver(solverID)
                    if (solver != null) {
                        op(solver, Theory.of(clauseList))
                        responseObserver.onNext(buildOperationResult())
                    } else {
                        responseObserver.onNext(buildOperationResult("The selected solver does not exist or  is not mutable"))
                    }
                    responseObserver.onCompleted()
                    DbManager.get().updateSolver(solverID)
                }
            }
        }
    }

    override fun loadLibrary(request: MutableLibrary, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.loadLibrary(request.library.parse())},
            responseObserver)
    }

    override fun assertA(request: MutableClause, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.assertA(request.clause.parseToStruct())},
            responseObserver)
    }

    override fun assertZ(request: MutableClause, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.assertZ(request.clause.parseToStruct())},
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
        genericRetract(request.solverID, request.clause, responseObserver) {
            solver, struct -> solver.retract(struct)
        }
    }

    override fun retractAll(request: MutableClause, responseObserver: StreamObserver<RetractResultMsg>) {
        genericRetract(request.solverID, request.clause, responseObserver) {
                solver, struct -> solver.retractAll(struct)
        }
    }

    private fun genericRetract(solverID: String, structToRetract: TheoryMsg.ClauseMsg,
                               responseObserver: StreamObserver<RetractResultMsg>,
                               operation: (MutableSolver, Struct) -> RetractResult<Theory>) {
        checkSolverExistence(solverID, responseObserver) {
            try {
                val result = operation(
                    solvers.getMutableSolver(solverID)!!,
                    structToRetract.parseToStruct()
                )
                val responseBuilder = RetractResultMsg.newBuilder().setTheory(result.theory.toMsg())
                    .addAllClauses(result.clauses?.map { it.toMsg() })
                if (result.isSuccess) {
                    responseObserver.onNext(responseBuilder.setIsSuccess(true).build())
                } else {
                    responseObserver.onNext(responseBuilder.setIsFailure(true).build())
                }
            } catch (e: Exception) {
                println(e)
            }
            responseObserver.onCompleted()
            DbManager.get().updateSolver(solverID)
        }
    }

    override fun setFlag(request: MutableFlag, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.setFlag( request.flag.parse())},
            responseObserver)
    }

    override fun setLibraries(request: MutableRuntime, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.setRuntime(request.runtime.parse()) },
            responseObserver)
    }

    override fun unloadLibrary(request: MutableLibrary, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            { it.unloadLibrary(request.library.parse()) },
            responseObserver)
    }

    override fun setChannel(request: MutableChannelID, responseObserver: StreamObserver<OperationResult>) {
        doOperationOnMutableSolver(request.solverID,
            {
                val collection = SolversCollection.getChannelDequesOfSolver(request.solverID)
                when(request.type) {
                (MutableChannelID.CHANNEL_TYPE.INPUT) -> {
                    val channel = collection.addInputChannel(InputStore.STDIN, request.channel.contentList)
                    it.setStandardInput(channel)
                }
                else -> {}
            }}, responseObserver)
    }

    private fun doOperationOnMutableSolver(solverID: String, operation: (MutableSolver) -> Unit, responseObserver: StreamObserver<OperationResult>) {
        checkSolverExistence(solverID, responseObserver) {
            try {
                operation(solvers.getMutableSolver(solverID)!!)
                responseObserver.onNext(buildOperationResult())
            } catch (e: Exception) {
                responseObserver.onNext(buildOperationResult("The selected solver does not exist or is not mutable"))
            }
            responseObserver.onCompleted()
            DbManager.get().updateSolver(solverID)
        }
    }

    private fun buildOperationResult(error: String = ""): OperationResult {
        return if(error.isEmpty()) OperationResult.newBuilder().setResult(true).build() else
            OperationResult.newBuilder().setResult(false).setError(error).build()
    }
}