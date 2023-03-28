package it.unibo.tuprolog.solve.lpaas.util

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.classic.stdlib.DefaultBuiltins
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.libs.io.IOLib
import it.unibo.tuprolog.solve.libs.oop.OOPLib
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentClient
import it.unibo.tuprolog.solve.lpaas.solveMessage.TheoryMsg
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import it.unibo.tuprolog.utils.Cached
import kotlinx.coroutines.CompletableDeferred

const val EAGER_OPTION = "eagerness"
const val TIMEOUT_OPTION = "timeout"
const val LIMIT_OPTION = "limit"
const val LAZY_OPTION = "laziness"

val DEFAULT_STATIC_THEORY_STRING = """
                f(b).
                f(d).
                """.trimIndent()

val DEFAULT_STATIC_THEORY = Theory.parse(DEFAULT_STATIC_THEORY_STRING)

private const val STRING_LENGTH = 10
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun idGenerator(): String {
    return List(STRING_LENGTH) { charPool.random() }.joinToString("")
}

fun <A, B> List<Pair<A, B>>.toMap(): MutableMap<A, B> {
    val map = mutableMapOf<A, B>()
    this.forEach { map[it.first] = it.second }
    return map
}

fun convertStringToKnownLibrary(libName: String): Library {
    return when(libName) {
        "prolog.io" -> IOLib
        "prolog.oop" -> OOPLib
        "prolog.lang" -> DefaultBuiltins
        else -> throw IllegalArgumentException()
    }
}


