package it.unibo.tuprolog.solve.lpaas.util


const val EAGER_OPTION = "eagerness"
const val TIMEOUT_OPTION = "timeout"
const val LIMIT_OPTION = "limit"
const val LAZY_OPTION = "laziness"

val DEFAULT_STATIC_THEORY = """
                f(b).
                f(d).
                """.trimIndent()

private const val STRING_LENGTH = 10
private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun idGenerator(): String {
    return List(STRING_LENGTH) { charPool.random() }.joinToString("")
}
