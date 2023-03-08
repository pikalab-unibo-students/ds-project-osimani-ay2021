package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.*
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.libs.io.IOLib
import it.unibo.tuprolog.solve.libs.oop.OOPLib
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.server.utils.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solveMessage.*
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*
import it.unibo.tuprolog.solve.lpaas.util.convertStringToKnownLibrary
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

import it.unibo.tuprolog.solve.lpaas.util.toMap

object SolverFactoryService: SolverFactoryGrpc.SolverFactoryImplBase() {

    private val solvers = SolversCollection

    override fun solverOf(request: SolverRequest, responseObserver: StreamObserver<SolverReply>) {
        val id = solvers.addSolver(
            ifEmptyUseDefault(request.unificator.substitutionList,
                { parseUnificator(it) }, Solver.prolog.defaultUnificator),
            ifEmptyUseDefault(request.runtime.librariesList,
                { parseRuntime(it) }, Solver.prolog.defaultRuntime),
            ifEmptyUseDefault(request.flags.flagsList,
                { parseFlagStore(it) }, Solver.prolog.defaultFlags),
            ifEmptyUseDefault(request.staticKb.clauseList,
                { parseTheory(it)}, Solver.prolog.defaultStaticKb),
            ifEmptyUseDefault(request.dynamicKb.clauseList,
                { parseTheory(it)}, Solver.prolog.defaultDynamicKb),
            ifEmptyUseDefault(request.inputStore.channelList,
                { parseInputChannels(it) }, InputStore.fromStandard().map { Pair(it.key, "") }.toMap()),
            ifEmptyUseDefault(request.outputStore.channelList,
                { parseOutputChannels(it) }, OutputStore.fromStandard().map { it.key }.toSet()),
            request.mutable, request.defaultBuiltIns)
        responseObserver.onNext(buildSolverReply(id))
        responseObserver.onCompleted()
    }
    private fun <A, B> ifEmptyUseDefault(value: List<A>, parser: (List<A>) -> B, default: B): B {
        return if(value.isEmpty()) default else parser(value)
    }

    private fun parseUnificator(msg: List<SubstitutionMsg>): Unificator {
        val scope = Scope.empty()
        return Unificator.strict(
            Substitution.of( msg.map { Pair(scope.varOf(it.`var`), Term.parse(it.term))}))
    }

    private fun parseRuntime(msg: List<RuntimeMsg.LibraryMsg>): Runtime {
        return Runtime.of(msg.map {
            convertStringToKnownLibrary(it.name)})
    }

    private fun parseFlagStore(msg: List<FlagsMsg.FlagMsg>): FlagStore {
        val flagMap = mutableMapOf<String, Term>()
        msg.forEach { flagMap[it.name] = Term.parse(it.value) }
        return FlagStore.of(flagMap)
    }

    private fun parseTheory(msg: List<TheoryMsg.ClauseMsg>): Theory {
        return Theory.of(msg.map { Clause.parse(it.content) })
    }

    private fun parseInputChannels(msg: List<Channels.ChannelID>): Map<String, String> {
        return msg.map { Pair(it.name, it.content) }.toMap()
    }

    private fun parseOutputChannels(msg: List<Channels.ChannelID>): Set<String> {
        return msg.map { it.name }.toSet()
    }

    private fun buildSolverReply(id: String): SolverReply {
        return SolverReply.newBuilder().setId(id).build()
    }
}