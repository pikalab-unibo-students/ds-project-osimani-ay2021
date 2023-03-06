package it.unibo.tuprolog.solve.lpaas.util

import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse


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
