package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.solve.Signature

data class DistributedRuntime(
    val libraries: Iterable<DistributedLibrary>
) {
    data class DistributedLibrary(
        val alias: String,
        val primitives: Set<Signature>,
        val rulesSignatures: Set<Signature>,
        val clauses: Set<Clause>,
        val functionSignatures: Set<Signature>
    )

    val aliases: Iterable<String>
        get() = libraries.map { it.alias }

    companion object {
         fun of(libraries: Iterable<DistributedLibrary>) =
             DistributedRuntime(libraries)
    }
}