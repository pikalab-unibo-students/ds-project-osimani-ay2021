package it.unibo.tuprolog.primitives.server.session

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import kotlinx.coroutines.flow.Flow

interface Session {

    suspend fun subSolve(query: Struct): Flow<Solution>

    suspend fun readLine(channelName: String): String

}
