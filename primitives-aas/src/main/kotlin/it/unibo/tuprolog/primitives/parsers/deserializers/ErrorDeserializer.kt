package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.ErrorMsg
import it.unibo.tuprolog.primitives.errors.LogicErrorMsg
import it.unibo.tuprolog.primitives.parsers.ParsingException
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.HaltException
import it.unibo.tuprolog.solve.exception.LogicError
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.exception.TimeOutException
import it.unibo.tuprolog.solve.exception.error.*
import it.unibo.tuprolog.solve.exception.warning.InitializationIssue
import it.unibo.tuprolog.solve.exception.warning.MissingPredicate

fun ErrorMsg.deserialize(scope: Scope = Scope.empty()): ResolutionException {
    val contexts = this.contextsList.map { it.deserialize(scope) }.toTypedArray()
    val message = if(this.hasMessage()) {
        this.message
    } else null
    val cause = if(this.hasCause()) {
        this.cause.deserialize(scope)
    } else null
    return when(this.errorCase) {
        ErrorMsg.ErrorCase.LOGICERROR -> {
            this.logicError.deserialize(message, cause, contexts)
        }
        ErrorMsg.ErrorCase.INITIALIZATIONISSUE -> {
            InitializationIssue(
                this.initializationIssue.goal.deserialize(),
                cause, contexts
            )
        }
        ErrorMsg.ErrorCase.MISSINGPREDICATE -> {
            MissingPredicate(cause, contexts, this.missingPredicate.signature.deserialize())
        }
        ErrorMsg.ErrorCase.HALTEXCEPTION -> {
            HaltException(this.haltException.exitStatus, message, cause, contexts)
        }
        ErrorMsg.ErrorCase.RESOLUTIONEXCEPTION -> {
            ResolutionException(message, cause, contexts)
        }
        ErrorMsg.ErrorCase.TIMEOUTEXCEPTION -> {
            TimeOutException(message, cause, contexts, this.timeoutException.exceededDuration)
        }
        else ->
            throw ParsingException(this)
    }
}

fun LogicErrorMsg.deserialize(message: String?, cause: Throwable?, contexts: Array<ExecutionContext>): LogicError {
    val extraData =
        if(this.extraData.isInitialized)
            this.extraData.deserialize()
        else null
    return when(this.errorCase) {
        LogicErrorMsg.ErrorCase.DOMAINERROR -> {
            val error = this.domainError
            DomainError(
                message, cause, contexts,
                DomainError.Expected.valueOf(error.expectedDomain),
                error.culprit.deserialize(),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.EVALUATIONERROR -> {
            EvaluationError(
                message, cause, contexts,
                EvaluationError.Type.valueOf(this.evaluationError.errorType),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.EXISTENCEERROR -> {
            val error = this.existenceError
            ExistenceError(
                message, cause, contexts,
                ExistenceError.ObjectType.valueOf(error.expectedObject),
                error.culprit.deserialize(),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.INSTANTIATIONERROR -> {
            InstantiationError(
                message, cause, contexts,
                this.instantiationError.culprit.deserialize().castToVar(),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.PERMISSIONERROR -> {
            val error = this.permissionError
            PermissionError(
                message, cause, contexts,
                PermissionError.Operation.valueOf(error.operation),
                PermissionError.Permission.valueOf(error.permission),
                this.permissionError.culprit.deserialize().castToVar(),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.REPRESENTATIONERROR -> {
            RepresentationError(
                message, cause, contexts,
                RepresentationError.Limit.valueOf(this.representationError.limit),
                extraData
            )
        }
        LogicErrorMsg.ErrorCase.SYNTAXERROR -> {
            SyntaxError(message, cause, contexts, extraData)
        }
        LogicErrorMsg.ErrorCase.SYSTEMERROR -> {
            SystemError(message, cause, contexts, extraData)
        }
        LogicErrorMsg.ErrorCase.TYPEERROR -> {
            val error = this.typeError
            TypeError(
                message, cause, contexts,
                TypeError.Expected.valueOf(error.expectedType),
                this.typeError.culprit.deserialize(),
                extraData
            )
        }
        else -> {
            LogicError.of(
                message, cause, contexts,
                type.deserialize().castToStruct(), extraData)
        }
    }
}