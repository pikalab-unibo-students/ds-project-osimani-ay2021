package it.unibo.tuprolog.solve.exception.prologerror

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitive.Signature
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.PrologError
import it.unibo.tuprolog.solve.exception.prologerror.TypeError.Expected

/**
 * The type error occurs when something is not of [Expected] type
 *
 * @param message the detail message string.
 * @param cause the cause of this exception.
 * @param context The current context at exception creation
 * @param expectedType The type expected, that wouldn't have raised the error
 * @param actualValue The value not respecting [expectedType]
 * @param extraData The possible extra data to be carried with the error
 *
 * @author Enrico
 */
class TypeError(
        message: String? = null,
        cause: Throwable? = null,
        context: ExecutionContext,
        val expectedType: Expected,
        val actualValue: Term,
        extraData: Term? = null
) : PrologError(message, cause, context, Atom.of(typeFunctor), extraData) {

    /** This constructor automatically fills [message] field with provided information */
    constructor(context: ExecutionContext, procedure: Signature, expectedType: Expected, actualValue: Term, index: Int? = null) : this(
            message = "Argument ${index
                    ?: ""} of `$procedure` should be a `$expectedType`, but `$actualValue` has been provided instead",
            context = context,
            expectedType = expectedType,
            actualValue = actualValue,
            extraData = actualValue
    )

    override val type: Struct by lazy { Struct.of(super.type.functor, expectedType.toAtom(), actualValue) }

    override fun updateContext(context: ExecutionContext): TypeError {
        return TypeError(message, cause, context, expectedType, actualValue, extraData)
    }

    companion object {

        /** The type error Struct functor */
        const val typeFunctor = "type_error"
    }

    /**
     * A class describing the expected type whose absence caused the error
     *
     * @param type the type expected string description
     *
     * @author Enrico
     */
    class Expected private constructor(private val type: String) {

        /** A function to transform the type to corresponding [Atom] representation */
        fun toAtom(): Atom = Atom.of(type)

        override fun toString(): String = type

        companion object {

            /** Predefined expected types Atom values */
            private val predefinedExpectedTypes by lazy {
                listOf("callable", "atom", "integer", "number", "predicate_indicator", "compound",
                        "list", "character")
                // these are only some of the commonly used types... when implementing more built-ins types can be added
                // maybe in future "type" information, as it is described in PrologStandard, could be moved in a standalone "enum class" and used here
            }

            /** Predefined expected instances */
            private val predefinedNameToInstance by lazy { predefinedExpectedTypes.map { it to Expected(it) }.toMap() }

            val CALLABLE by lazy { predefinedNameToInstance.getValue("callable") }
            val ATOM by lazy { predefinedNameToInstance.getValue("atom") }
            val INTEGER by lazy { predefinedNameToInstance.getValue("integer") }
            val NUMBER by lazy { predefinedNameToInstance.getValue("number") }
            val PREDICATE_INDICATOR by lazy { predefinedNameToInstance.getValue("predicate_indicator") }
            val COMPOUND by lazy { predefinedNameToInstance.getValue("compound") }
            val LIST by lazy { predefinedNameToInstance.getValue("list") }
            val CHARACTER by lazy { predefinedNameToInstance.getValue("character") }

            /** Returns the Expected instance described by [type]; creates a new instance only if [type] was not predefined */
            fun of(type: String): Expected = predefinedNameToInstance[type.toLowerCase()]
                    ?: Expected(type)

            /** Gets [Expected] instance from [term] representation, if possible */
            fun fromTerm(term: Term): Expected? = when (term) {
                is Atom -> of(term.value)
                else -> null
            }
        }
    }


}