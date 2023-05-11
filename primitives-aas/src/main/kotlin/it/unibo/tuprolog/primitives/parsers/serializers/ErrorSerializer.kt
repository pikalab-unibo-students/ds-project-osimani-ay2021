package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.errors.*
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.solve.exception.*
import it.unibo.tuprolog.solve.exception.error.*
import it.unibo.tuprolog.solve.exception.warning.InitializationIssue
import it.unibo.tuprolog.solve.exception.warning.MissingPredicate

fun ResolutionException.serialize(): ErrorMsg {
    val builder = ErrorMsg.newBuilder()
        .addAllContexts(this.contexts.map { it.serialize() })
    this.message?.let {
        builder.setMessage(this.message)
    }
    this.cause?.let {
        builder.setCause(
            if(it is ResolutionException) it.serialize()
            else ErrorMsg.newBuilder().setMessage(it.message ).build()
        )}
    return when (this) {
        is LogicError -> this.serialize(builder)
        is Warning -> this.serialize(builder)
        is HaltException ->
            builder.setHaltException(
                HaltExceptionMsg.newBuilder()
                    .setExitStatus(this.exitStatus)).build()
        is TimeOutException ->
            builder.setTimeoutException(
                TimeOutExceptionMsg.newBuilder()
                    .setExceededDuration(this.exceededDuration)).build()
        else -> builder.setResolutionException(ResolutionExceptionMsg.getDefaultInstance()).build()
    }
}

fun LogicError.serialize(builder: ErrorMsg.Builder): ErrorMsg {
    val logicErrorBuilder = LogicErrorMsg.newBuilder()
        .setType(this.type.serialize())
        .setExtraData(this.extraData?.serialize())
    when (this) {
        is DomainError ->
            logicErrorBuilder.setDomainError(
                DomainErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedDomain(this.expectedDomain.domain)
            )
        is EvaluationError ->
            logicErrorBuilder.setEvaluationError(
                EvaluationErrorMsg.newBuilder()
                    .setErrorType(this.errorType.name)
            )
        is ExistenceError ->
            logicErrorBuilder.setExistenceError(
                ExistenceErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedObject(this.expectedObject.name)
            )
        is InstantiationError ->
            logicErrorBuilder.setInstantiationError(
                InstantiationErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
            )
        is MessageError ->
            logicErrorBuilder.setMessageError(
                MessageErrorMsg.getDefaultInstance()
            )
        is PermissionError ->
            logicErrorBuilder.setPermissionError(
                PermissionErrorMsg.newBuilder()
                    .setOperation(this.operation.operation)
                    .setPermission(this.permission.permission)
                    .setCulprit(this.culprit.serialize())
            )
        is RepresentationError ->
            logicErrorBuilder.setRepresentationError(
                RepresentationErrorMsg.newBuilder()
                    .setLimit(this.limit.limit)
            )
        is SyntaxError ->
            logicErrorBuilder.setSyntaxError(
                SyntaxErrorMsg.getDefaultInstance()
            )
        is SystemError ->
            logicErrorBuilder.setSystemError(
                SystemErrorMsg.getDefaultInstance()
            )
        is TypeError ->
            logicErrorBuilder.setTypeError(
                TypeErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedType(this.expectedType.name)
            )
        else -> throw ParsingException(this)
    }
    return builder.setLogicError(logicErrorBuilder).build()
}

fun Warning.serialize(builder: ErrorMsg.Builder): ErrorMsg =
    when(this) {
        is InitializationIssue ->
            builder.setInitializationIssue(
                InitializationIssueMsg.newBuilder()
                    .setGoal(this.goal.serialize())).build()
        is MissingPredicate ->
            builder.setMissingPredicate(
                MissingPredicateMsg.newBuilder()
                    .setSignature(this.signature.serialize())).build()
        else -> throw ParsingException(this)
    }