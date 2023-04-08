package it.unibo.tuprolog.solve.lpaas.server.services

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import it.unibo.tuprolog.solve.lpaas.server.collections.ComputationsCollection
import it.unibo.tuprolog.solve.lpaas.server.collections.SolversCollection
import it.unibo.tuprolog.solve.lpaas.server.database.DbManager
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.util.EAGER_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LAZY_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LIMIT_OPTION
import it.unibo.tuprolog.solve.lpaas.util.TIMEOUT_OPTION
import it.unibo.tuprolog.solve.lpaas.util.parsers.MessageBuilder
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverDeserializer.deserializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.serializer
import it.unibo.tuprolog.solve.lpaas.util.parsers.SolverSerializer.toMsg
import kotlinx.coroutines.runBlocking

object SolverService : SolverGrpc.SolverImplBase() {

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionSequence>) {
        val computationID = ComputationsCollection.addIterator(request.solverID,
            deserializer.deserialize(request.struct).castToStruct(),
            parseOptions(request.optionsList)
        )
        responseObserver.onNext(
            SolutionSequence.newBuilder().setSolverID(request.solverID)
                .setComputationID(computationID).setQuery(request.struct).build()
        )
        responseObserver.onCompleted()
        DbManager.get().updateSolver(request.solverID)
    }

    override fun getSolution(request: SolutionID, responseObserver: StreamObserver<SolutionReply>) {
        val message: SolutionReply = try {
            val solution = runBlocking {
                ComputationsCollection.getSolution(request.solverID, request.computationID,
                    deserializer.deserialize(request.query).castToStruct(),
                    request.index)
            }
            buildSolutionReply(solution.first, solution.second)
        } catch (e: Error) {
            SolutionReply.newBuilder().setQuery(request.query).setIsNo(true).setError(
                SolutionReply.ErrorMsg.newBuilder().setMessage(e.toString())
            ).build()
        }
        responseObserver.onNext( message )
        responseObserver.onCompleted()
        DbManager.get().updateSolver(request.solverID)
    }

    override fun writeOnInputChannel(request: InputChannelEvent, responseObserver: StreamObserver<OperationResult>) {
        request.lineList.forEach {
            SolversCollection.getChannelDequesOfSolver(request.solverID)
                .writeOnInputChannel(request.channelID.name, it)
        }
        responseObserver.onNext(OperationResult.newBuilder().setResult(true).build())
        responseObserver.onCompleted()
        DbManager.get().updateSolver(request.solverID)
    }

    override fun readFromOutputChannel(request: OutputChannelEvent, responseObserver: StreamObserver<ReadLine>) {
        val outputValue = SolversCollection.getChannelDequesOfSolver(request.solverID)
            .consumeFromOutputChannel(request.channelID.name)
        responseObserver.onNext(MessageBuilder.fromReadLineToMsg(outputValue))
        responseObserver.onCompleted()
        DbManager.get().updateSolver(request.solverID)
    }

    override fun readStreamFromOutputChannel(responseObserver: StreamObserver<ReadLine>):
        StreamObserver<OutputChannelEvent> {
        return object: StreamObserver<OutputChannelEvent> {
            var solverID = ""
            var channelID = ""
            override fun onNext(value: OutputChannelEvent) {
                solverID = value.solverID
                channelID = value.channelID.name
                SolversCollection.getChannelDequesOfSolver(solverID)
                    .addListener(channelID, responseObserver)
            }
            override fun onError(t: Throwable?) {}
            override fun onCompleted() {
                SolversCollection.getChannelDequesOfSolver(solverID)
                    .removeListener(channelID, responseObserver)
            }
        }
    }

    override fun getFlags(request: SolverID, responseObserver: StreamObserver<FlagsMsg>) {
        responseObserver.onNext(SolversCollection.getSolver(request.solverID).flags.toMsg())
        responseObserver.onCompleted()
    }

    override fun getStaticKB(request: SolverID, responseObserver: StreamObserver<TheoryMsg.ClauseMsg>) {
        SolversCollection.getSolver(request.solverID).staticKb.clauses.forEach {
            responseObserver.onNext(it.toMsg())
        }
        responseObserver.onCompleted()
    }

    override fun getDynamicKB(request: SolverID, responseObserver: StreamObserver<TheoryMsg.ClauseMsg>) {
        SolversCollection.getSolver(request.solverID).dynamicKb.clauses.forEach {
            responseObserver.onNext(it.toMsg())
        }
        responseObserver.onCompleted()
    }

    override fun getLibraries(request: SolverID, responseObserver: StreamObserver<RuntimeMsg>) {
        responseObserver.onNext(SolversCollection.getSolver(request.solverID).libraries.toMsg())
        responseObserver.onCompleted()
    }

    override fun getUnificator(request: SolverID, responseObserver: StreamObserver<UnificatorMsg>) {
        responseObserver.onNext(SolversCollection.getSolver(request.solverID).unificator.toMsg())
        responseObserver.onCompleted()
    }

    override fun getOperators(request: SolverID, responseObserver: StreamObserver<OperatorSetMsg>) {
        responseObserver.onNext(SolversCollection.getSolver(request.solverID).operators.toMsg())
        responseObserver.onCompleted()
    }

    override fun getInputChannels(request: SolverID, responseObserver: StreamObserver<Channels>) {
        val messageBuilder = Channels.newBuilder()
        SolversCollection.getChannelDequesOfSolver(request.solverID).getInputChannels().forEach {
            messageBuilder.addChannel(
                MessageBuilder.fromChannelIDToMsg(it.key,
                    it.value.getCurrentContent()))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getOutputChannels(request: SolverID, responseObserver: StreamObserver<Channels>) {
        val messageBuilder = Channels.newBuilder()
        SolversCollection.getChannelDequesOfSolver(request.solverID).getOutputChannels().forEach {
            messageBuilder.addChannel(
                MessageBuilder.fromChannelIDToMsg(it.key, it.value.getCurrentContent()))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    private fun parseOptions(options: List<SolveRequest.Options>): SolveOptions {
        var laziness = true
        var limit = SolveOptions.ALL_SOLUTIONS
        var timeout = SolveOptions.MAX_TIMEOUT
        SolveOptions.DEFAULT
        options.forEach {
            when(it.name) {
                TIMEOUT_OPTION -> timeout = it.value
                LIMIT_OPTION -> limit = it.value.toInt()
                LAZY_OPTION -> laziness = true
                EAGER_OPTION -> laziness = false
            }
        }
        return SolveOptions.of(laziness, timeout, limit)
    }

    private fun buildSolutionReply(solution: Solution, hasNext: Boolean): SolutionReply {
        val solutionBuilder = SolutionReply.newBuilder()
            .setQuery(solution.query.toString())
            .setIsYes(solution.isYes)
            .setIsNo(solution.isNo)
            .setIsHalt(solution.isHalt)
            .setHasNext(hasNext)
        if(solution.substitution.isSuccess) {
            solution.substitution.asIterable().forEach {
                solutionBuilder.addSubstitution(
                    SubstitutionMsg.newBuilder()
                    .setVar(serializer.serialize(it.key))
                    .setTerm(serializer.serialize(it.value)))
            }
        }

        if(solution.exception != null) {
            val error = solution.exception!!
            solutionBuilder.error = SolutionReply.ErrorMsg.newBuilder()
                .setMessage(error.toString())
                .addAllLogicStackTrace(error.logicStackTrace.map { serializer.serialize(it)})
                .addAllCustomDataStore(MessageBuilder.fromCustomDataStoreToMsg(error.context.customData))
                .setStartTime(error.context.startTime)
                .setMaxDuration(error.context.maxDuration)
                .build()
        }
        return solutionBuilder.build()
    }

    override fun deleteSolver(request: SolverID, responseObserver: StreamObserver<OperationResult>) {
        SolversCollection.deleteSolver(request.solverID)
        responseObserver.onNext(OperationResult.newBuilder().setResult(true).build())
        responseObserver.onCompleted()
    }
}