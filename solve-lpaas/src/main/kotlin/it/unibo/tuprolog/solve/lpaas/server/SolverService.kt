package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.server.utils.ComputationsCollection
import it.unibo.tuprolog.solve.lpaas.server.utils.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solveMessage.RuntimeMsg.LibraryMsg
import it.unibo.tuprolog.solve.lpaas.util.EAGER_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LAZY_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LIMIT_OPTION
import it.unibo.tuprolog.solve.lpaas.util.TIMEOUT_OPTION
import kotlinx.coroutines.runBlocking

object SolverService : SolverGrpc.SolverImplBase() {

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionSequence>) {
        val computationID = ComputationsCollection.addIterator(request.solverID, request.struct,
            parseOptions(request.optionsList))
        responseObserver.onNext(
            SolutionSequence.newBuilder().setSolverID(request.solverID)
                .setComputationID(computationID).setQuery(request.struct).build()
        )
        responseObserver.onCompleted()
    }

    override fun getSolution(request: SolutionID, responseObserver: StreamObserver<SolutionReply>) {
        val message: SolutionReply = try {
            val solution = runBlocking {
                ComputationsCollection.getSolution(request.solverID, request.computationID,
                    request.query, request.index)
            }
            buildSolutionReply(solution)
        } catch (e: Error) {
            SolutionReply.newBuilder().setQuery(request.query).setIsNo(true).setError(e.toString()).build()
        }
        responseObserver.onNext( message )
        responseObserver.onCompleted()
    }

    override fun writeOnInputChannel(responseObserver: StreamObserver<LineEvent>): StreamObserver<LineEvent> {
        return object: StreamObserver<LineEvent> {

            override fun onNext(value: LineEvent) {
                SolversCollection.getChannelDequesOfSolver(value.solverID)
                    .writeOnInputChannel(value.channelID.name, value.line)
            }

            override fun onError(t: Throwable?) {}
            override fun onCompleted() {}
        }
    }

    override fun readFromOutputChannel(request: OutputChannelEvent, responseObserver: StreamObserver<LineEvent>) {
        val outputValue = SolversCollection.getChannelDequesOfSolver(request.solverID)
            .readFromOutputChannel(request.channelID.name)
        responseObserver.onNext(LineEvent.newBuilder().setLine(outputValue)
            .setSolverID(request.solverID).setChannelID(request.channelID).build())
        responseObserver.onCompleted()
    }

    override fun getFlags(request: SolverID, responseObserver: StreamObserver<FlagsMsg>) {
        val messageBuilder = FlagsMsg.newBuilder()
        SolversCollection.getSolver(request.solverID).flags.forEach {
            messageBuilder.addFlags(
                FlagsMsg.FlagMsg.newBuilder().setName(it.key).setValue(it.value.toString()))}
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getStaticKB(request: SolverID, responseObserver: StreamObserver<TheoryMsg.ClauseMsg>) {
        SolversCollection.getSolver(request.solverID).staticKb.clauses.forEach {
            responseObserver.onNext(TheoryMsg.ClauseMsg.newBuilder().setContent(it.toString()).build())
        }
        responseObserver.onCompleted()
    }

    override fun getDynamicKB(request: SolverID, responseObserver: StreamObserver<TheoryMsg.ClauseMsg>) {
        SolversCollection.getSolver(request.solverID).dynamicKb.clauses.forEach {
            responseObserver.onNext(TheoryMsg.ClauseMsg.newBuilder().setContent(it.toString()).build())
        }
        responseObserver.onCompleted()
    }

    override fun getLibraries(request: SolverID, responseObserver: StreamObserver<RuntimeMsg>) {
        val messageBuilder = RuntimeMsg.newBuilder()
        SolversCollection.getSolver(request.solverID).libraries.forEach {
            messageBuilder.addLibraries(LibraryMsg.newBuilder().setName(it.key))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getUnificator(request: SolverID, responseObserver: StreamObserver<UnificatorMsg>) {
        val messageBuilder = UnificatorMsg.newBuilder()
        SolversCollection.getSolver(request.solverID).unificator.context.forEach {
            messageBuilder.addSubstitution(SubstitutionMsg.newBuilder()
                .setVar(it.key.toString()).setTerm(it.value.toString()))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getOperators(request: SolverID, responseObserver: StreamObserver<OperatorSetMsg>) {
        val messageBuilder = OperatorSetMsg.newBuilder()
        SolversCollection.getSolver(request.solverID).operators.forEach {
            messageBuilder.addOperator(OperatorSetMsg.OperatorMsg.newBuilder()
                .setFunctor(it.functor).setSpecifier(it.specifier.name).setPriority(it.priority))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getInputChannels(request: SolverID, responseObserver: StreamObserver<Channels>) {
        val messageBuilder = Channels.newBuilder()
        SolversCollection.getSolver(request.solverID).inputChannels.forEach {
            messageBuilder.addChannel(Channels.ChannelID.newBuilder().setName(it.key))
        }
        responseObserver.onNext(messageBuilder.build())
        responseObserver.onCompleted()
    }

    override fun getOutputChannels(request: SolverID, responseObserver: StreamObserver<Channels>) {
        val messageBuilder = Channels.newBuilder()
        SolversCollection.getSolver(request.solverID).outputChannels.forEach {
            messageBuilder.addChannel(Channels.ChannelID.newBuilder().setName(it.key))
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

    private fun buildSolutionReply(solution: Solution): SolutionReply {
        val solutionBuilder = SolutionReply.newBuilder()
            .setQuery(solution.query.toString())
            .setIsYes(solution.isYes)
            .setIsNo(solution.isNo)
            .setIsHalt(solution.isHalt)
        if(solution.substitution.isSuccess) {
            solution.substitution.asIterable().forEach {
                solutionBuilder.addSubstitution(
                    SubstitutionMsg.newBuilder()
                    .setVar(it.key.name)
                    .setTerm(it.value.toString()))
            }
        }
        if(solution.exception != null)
            solutionBuilder.error = solution.exception.toString()
        return solutionBuilder.build()
    }
}