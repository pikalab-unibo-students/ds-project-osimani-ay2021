package it.unibo.tuprolog.primitives.parsers.serializers.distribuited

import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.errors.*
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError

fun DistributedError.serialize(): ErrorMsg {
    val builder = ErrorMsg.newBuilder()
    this.message?.let {
        builder.setMessage(this.message)
    }
    this.cause?.let {
        builder.setCause(
            if(it is DistributedError.ResolutionException) it.serialize()
            else ErrorMsg.newBuilder().setMessage(it.message ).build()
        )}
    return when (this) {
        is DistributedError.LogicError -> this.serialize(builder)
        is DistributedError.InitializationIssue ->
            builder.setInitializationIssue(
                InitializationIssueMsg.newBuilder()
                    .setGoal(this.goal.serialize())).build()
        is DistributedError.MissingPredicate ->
            builder.setMissingPredicate(
                MissingPredicateMsg.newBuilder()
                    .setSignature(this.signature.serialize())).build()
        is DistributedError.HaltException ->
            builder.setHaltException(
                HaltExceptionMsg.newBuilder()
                    .setExitStatus(this.exitStatus)).build()
        is DistributedError.TimeOutException ->
            builder.setTimeoutException(
                TimeOutExceptionMsg.newBuilder()
                    .setExceededDuration(this.exceededDuration)).build()
        else -> builder.setResolutionException(ResolutionExceptionMsg.getDefaultInstance()).build()
    }
}

fun DistributedError.LogicError.serialize(builder: ErrorMsg.Builder): ErrorMsg {
    val logicErrorBuilder = LogicErrorMsg.newBuilder()
        .setType(this.type.serialize())
        .setExtraData(this.extraData?.serialize())
    when (this) {
        is DistributedError.DomainError ->
            logicErrorBuilder.setDomainError(
                DomainErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedDomain(this.expected.domain)
            )
        is DistributedError.EvaluationError ->
            logicErrorBuilder.setEvaluationError(
                EvaluationErrorMsg.newBuilder()
                    .setErrorType(this.errorType.name)
            )
        is DistributedError.ExistenceError ->
            logicErrorBuilder.setExistenceError(
                ExistenceErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedObject(this.expectedObjectType.name)
            )
        is DistributedError.InstantiationError ->
            logicErrorBuilder.setInstantiationError(
                InstantiationErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
            )
        is DistributedError.MessageError ->
            logicErrorBuilder.setMessageError(
                MessageErrorMsg.getDefaultInstance()
            )
        is DistributedError.PermissionError ->
            logicErrorBuilder.setPermissionError(
                PermissionErrorMsg.newBuilder()
                    .setOperation(this.operation.operation)
                    .setPermission(this.permission.permission)
                    .setCulprit(this.culprit.serialize())
            )
        is DistributedError.RepresentationError ->
            logicErrorBuilder.setRepresentationError(
                RepresentationErrorMsg.newBuilder()
                    .setLimit(this.limit.limit)
            )
        is DistributedError.SyntaxError ->
            logicErrorBuilder.setSyntaxError(
                SyntaxErrorMsg.getDefaultInstance()
            )
        is DistributedError.SystemError ->
            logicErrorBuilder.setSystemError(
                SystemErrorMsg.getDefaultInstance()
            )
        is DistributedError.TypeError ->
            logicErrorBuilder.setTypeError(
                TypeErrorMsg.newBuilder()
                    .setCulprit(this.culprit.serialize())
                    .setExpectedType(this.expectedType.name)
            )
        else -> throw ParsingException(this)
    }
    return builder.setLogicError(logicErrorBuilder).build()
}