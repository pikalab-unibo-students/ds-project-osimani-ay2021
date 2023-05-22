package it.unibo.tuprolog.primitives.parsers.deserializers

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.messages.ExecutionContextMsg
import it.unibo.tuprolog.primitives.utils.DummyContext
import it.unibo.tuprolog.solve.ExecutionContext

fun ExecutionContextMsg.deserialize(
    scope: Scope = Scope.empty(),
): ExecutionContext {
    val source = this
    return object : DummyContext() {
        override val procedure = source.procedure.deserialize(scope)
        override val substitution = Substitution.of(source.substitutionsMap.map
        {
            Pair(deserializeVar(it.key, scope), it.value.deserialize(scope))
        }.toMap())
        override val startTime = source.startTime
        override val endTime = source.endTime
        override val remainingTime = source.remainingTime
        override val elapsedTime = source.elapsedTime
        override val maxDuration = source.maxDuration
    }
}





