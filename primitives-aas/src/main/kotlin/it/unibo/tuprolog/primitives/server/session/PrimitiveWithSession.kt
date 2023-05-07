package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.flow.Flow

fun interface PrimitiveWithSession {

    suspend fun solve(request: Solve.Request<ExecutionContext>, session: Session): Sequence<Solve.Response>

    companion object {
        @JvmStatic
        fun of(function: (Solve.Request<ExecutionContext>, session: Session)
            -> Sequence<Solve.Response>): PrimitiveWithSession = PrimitiveWithSession(function)

    }
}