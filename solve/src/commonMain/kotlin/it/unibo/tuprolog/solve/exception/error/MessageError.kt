package it.unibo.tuprolog.solve.exception.error

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.PrologError

/**
 * The [MessageError] is used whenever no other [PrologError] instance is suitable for representing the error
 *
 * @param message the detail message string.
 * @param cause the cause of this exception.
 * @param contexts a stack of contexts localising the exception
 * @param extraData The possible extra data to be carried with the error
 *
 * @author Giovanni
 */
class MessageError internal constructor( // TODO: 16/01/2020 test this class
    message: String? = null,
    cause: Throwable? = null,
    contexts: Array<ExecutionContext>,
    extraData: Term? = null
) : PrologError(message, cause, contexts, Atom.of(typeFunctor), extraData) {

    constructor(
        message: String? = null,
        cause: Throwable? = null,
        context: ExecutionContext,
        extraData: Term? = null
    ) : this(message, cause, arrayOf(context), extraData)

    /** The content of this message error */
    val content: Term by lazy { extraData ?: errorStruct }

    override fun pushContext(newContext: ExecutionContext): MessageError =
        MessageError(message, cause, contexts.addLast(newContext), extraData)

    override fun updateContext(newContext: ExecutionContext): PrologError =
        MessageError(message, cause, contexts.setFirst(newContext), extraData)

    companion object {

        /** The message error Struct functor */
        const val typeFunctor = ""

        /** Factory method to create a [MessageError] */
        fun of(content: Term, context: ExecutionContext, cause: Throwable? = null) =
            MessageError(content.toString(), cause, context, content)

    }
}