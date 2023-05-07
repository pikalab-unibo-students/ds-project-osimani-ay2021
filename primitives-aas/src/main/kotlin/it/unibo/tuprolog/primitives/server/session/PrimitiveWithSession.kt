package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

fun interface PrimitiveWithSession {

    fun solve(request: Solve.Request<ExecutionContext>, session: ServerSession): Sequence<Solve.Response>

    companion object {
        @JvmStatic
        fun of(function: (Solve.Request<ExecutionContext>, session: ServerSession)
            -> Sequence<Solve.Response>): PrimitiveWithSession = PrimitiveWithSession(function)

    }
}