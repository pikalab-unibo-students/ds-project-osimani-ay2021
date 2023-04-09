package it.unibo.tuprolog.solve.lpaas.server.services

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.lpaas.SolverFactoryGrpc
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.server.database.DbManager
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels
import it.unibo.tuprolog.solve.lpaas.solveMessage.OperationResult
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverId
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.SolverRequest
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.parse
import it.unibo.tuprolog.solve.lpaas.util.toMap

object SolverFactoryService: SolverFactoryGrpc.SolverFactoryImplBase() {

    private val solvers = SolversCollection

    override fun solverOf(request: SolverRequest, responseObserver: StreamObserver<SolverId>) {
        val id = solvers.addSolver(
            ifEmptyUseDefault(request.unificator.substitutionList,
                { it.parse() }, Solver.prolog.defaultUnificator),
            ifEmptyUseDefault(request.runtime.librariesList,
                { it.parse() }, Solver.prolog.defaultRuntime),
            ifEmptyUseDefault(request.flags.flagsList,
                { it.parse() }, Solver.prolog.defaultFlags),
            ifEmptyUseDefault(request.staticKb.clauseList,
                { it.parse() }, Solver.prolog.defaultStaticKb),
            ifEmptyUseDefault(request.dynamicKb.clauseList,
                { it.parse() }, Solver.prolog.defaultDynamicKb),
            ifEmptyUseDefault(request.inputStore.channelList,
                { parseChannels(it) }, InputStore.fromStandard().map { Pair(it.key, emptyList<String>()) }.toMap()),
            ifEmptyUseDefault(request.outputStore.channelList,
                { parseChannels(it) }, OutputStore.fromStandard().map { Pair(it.key, emptyList<String>()) }.toMap()),
            request.mutable, request.defaultBuiltIns)
        responseObserver.onNext(buildSolverId(id))
        responseObserver.onCompleted()
        DbManager.get().addSolver(solverID = id, mutable = request.mutable)
    }

    override fun connectToSolver(request: SolverId, responseObserver: StreamObserver<OperationResult>) {
        checkSolverExistence(request.id, responseObserver) {
            responseObserver.onNext(
                OperationResult.newBuilder().setResult(true).build()
            )
            responseObserver.onCompleted()
        }
    }

    private fun <A, B> ifEmptyUseDefault(value: List<A>, parser: (List<A>) -> B, default: B): B {
        return if(value.isEmpty()) default else parser(value)
    }

    private fun parseChannels(msg: List<Channels.ChannelID>): Map<String, List<String>> {
        return msg.map { Pair(it.name, it.contentList) }.toMap()
    }

    private fun buildSolverId(id: String): SolverId {
        return SolverId.newBuilder().setId(id).build()
    }
}