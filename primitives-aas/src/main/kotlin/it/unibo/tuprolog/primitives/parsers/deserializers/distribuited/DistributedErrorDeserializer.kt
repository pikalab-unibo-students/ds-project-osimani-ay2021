package it.unibo.tuprolog.primitives.parsers.deserializers.distribuited

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.errors.LogicErrorMsg
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.solve.exception.error.*

fun ErrorMsg.deserializeAsDistributed(scope: Scope = Scope.empty()): DistributedError {
    val message = if(this.hasMessage()) {
        this.message
    } else null
    val cause = if(this.hasCause()) {
        this.cause.deserializeAsDistributed(scope)
    } else null
    return when(this.errorCase) {
        ErrorMsg.ErrorCase.LOGICERROR -> {
            this.logicError.deserializeAsDistributed(message, cause)
        }
        ErrorMsg.ErrorCase.INITIALIZATIONISSUE -> {
            DistributedError.InitializationIssue(
                message, cause, this.initializationIssue.goal.deserialize()
            )
        }
        ErrorMsg.ErrorCase.MISSINGPREDICATE -> {
            DistributedError.MissingPredicate(
                message, cause, this.missingPredicate.signature.deserialize())
        }
        ErrorMsg.ErrorCase.HALTEXCEPTION -> {
            DistributedError.HaltException(
                message, cause, this.haltException.exitStatus)
        }
        ErrorMsg.ErrorCase.RESOLUTIONEXCEPTION -> {
            DistributedError.ResolutionException(message, cause)
        }
        ErrorMsg.ErrorCase.TIMEOUTEXCEPTION -> {
            DistributedError.TimeOutException(message, cause, this.timeoutException.exceededDuration)
        }
        else ->
            throw ParsingException(this)
    }
}

fun LogicErrorMsg.deserializeAsDistributed(
    message: String?,
    cause: Throwable?,
): DistributedError {
    val extraData =
        if(this.extraData.isInitialized)
            this.extraData.deserialize()
        else null
    return when(this.errorCase) {
        LogicErrorMsg.ErrorCase.DOMAINERROR -> {
            val error = this.domainError
            DistributedError.DomainError(
                message,
                cause,
                extraData,
                DomainError.Expected.valueOf(error.expectedDomain),
                error.culprit.deserialize()
            )
        }
        LogicErrorMsg.ErrorCase.EVALUATIONERROR -> {
            DistributedError.EvaluationError(
                message,
                cause,
                extraData,
                EvaluationError.Type.valueOf(this.evaluationError.errorType)
            )
        }
        LogicErrorMsg.ErrorCase.EXISTENCEERROR -> {
            val error = this.existenceError
            DistributedError.ExistenceError(
                message,
                cause,
                extraData,
                ExistenceError.ObjectType.valueOf(error.expectedObject),
                error.culprit.deserialize()
            )
        }
        LogicErrorMsg.ErrorCase.INSTANTIATIONERROR -> {
            DistributedError.InstantiationError(
                message,
                cause,
                extraData,
                this.instantiationError.culprit.deserialize().castToVar()
            )
        }
        LogicErrorMsg.ErrorCase.PERMISSIONERROR -> {
            val error = this.permissionError
            DistributedError.PermissionError(
                message,
                cause,
                extraData,
                PermissionError.Operation.valueOf(error.operation),
                PermissionError.Permission.valueOf(error.permission),
                this.permissionError.culprit.deserialize().castToVar()
            )
        }
        LogicErrorMsg.ErrorCase.REPRESENTATIONERROR -> {
            DistributedError.RepresentationError(
                message,
                cause,
                extraData,
                RepresentationError.Limit.valueOf(this.representationError.limit)
            )
        }
        LogicErrorMsg.ErrorCase.SYNTAXERROR -> {
            DistributedError.SyntaxError(message, cause, extraData)
        }
        LogicErrorMsg.ErrorCase.SYSTEMERROR -> {
            DistributedError.SystemError(message, cause, extraData)
        }
        LogicErrorMsg.ErrorCase.TYPEERROR -> {
            val error = this.typeError
            DistributedError.TypeError(
                message,
                cause,
                extraData,
                TypeError.Expected.valueOf(error.expectedType),
                this.typeError.culprit.deserialize()
            )
        }
        else -> {
            DistributedError.LogicError(
                message,
                cause,
                type.deserialize().castToStruct(),
                extraData)
        }
    }
}