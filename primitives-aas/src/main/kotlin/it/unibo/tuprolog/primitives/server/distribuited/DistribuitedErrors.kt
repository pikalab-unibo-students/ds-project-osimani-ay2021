package it.unibo.tuprolog.primitives.server.distribuited

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.exception.TuPrologException
import it.unibo.tuprolog.solve.Signature

sealed class DistributedError(
    message: String? = null,
    cause: Throwable? = null
): TuPrologException(message, cause) {

    open class LogicError(
        message: String? = null,
        cause: Throwable? = null,
        val type: Struct,
        val extraData: Term? = null
        ): DistributedError(message, cause)

    class DomainError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val expected: it.unibo.tuprolog.solve.exception.error.DomainError.Expected,
        val culprit: Term
    ): LogicError(message, cause, Atom.of("domain_error"), extraData)

    class EvaluationError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val errorType: it.unibo.tuprolog.solve.exception.error.EvaluationError.Type
    ): LogicError(message, cause, Atom.of("evaluation_error"), extraData)

    class ExistenceError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val expectedObjectType: it.unibo.tuprolog.solve.exception.error.ExistenceError.ObjectType,
        val culprit: Term
    ): LogicError(message, cause, Atom.of("existence_error"), extraData)

    class InstantiationError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val culprit: Term
    ): LogicError(message, cause, Atom.of("instantiation_error"), extraData)

    class MessageError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
    ): LogicError(message, cause, Atom.of("message_error"), extraData)

    class PermissionError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val operation: it.unibo.tuprolog.solve.exception.error.PermissionError.Operation,
        val permission: it.unibo.tuprolog.solve.exception.error.PermissionError.Permission,
        val culprit: Term
    ): LogicError(message, cause, Atom.of("permission_error"), extraData)

    class RepresentationError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val limit: it.unibo.tuprolog.solve.exception.error.RepresentationError.Limit,
    ): LogicError(message, cause, Atom.of("representation_error"), extraData)

    class SyntaxError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
    ): LogicError(message, cause, Atom.of("syntax_error"), extraData)

    class SystemError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
    ): LogicError(message, cause, Atom.of("system_error"), extraData)

    class TypeError(
        message: String? = null,
        cause: Throwable? = null,
        extraData: Term? = null,
        val expectedType: it.unibo.tuprolog.solve.exception.error.TypeError.Expected,
        val culprit: Term
    ): LogicError(message, cause, Atom.of("type_error"), extraData)

    class InitializationIssue(
        message: String? = null,
        cause: Throwable? = null,
        val goal: Struct
    ): DistributedError(message, cause)

    class MissingPredicate(
        message: String? = null,
        cause: Throwable? = null,
        val signature: Signature
    ): DistributedError(message, cause)

    class HaltException(
        message: String? = null,
        cause: Throwable? = null,
        val exitStatus: Int
    ): DistributedError(message, cause)

    class ResolutionException(
        message: String? = null,
        cause: Throwable? = null,
    ): DistributedError(message, cause)

    class TimeOutException(
        message: String? = null,
        cause: Throwable? = null,
        val exceededDuration: Long
    ): DistributedError(message, cause)

}