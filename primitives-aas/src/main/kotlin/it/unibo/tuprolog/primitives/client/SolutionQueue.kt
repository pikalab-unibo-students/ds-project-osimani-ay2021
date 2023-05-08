package it.unibo.tuprolog.primitives.client

import it.unibo.tuprolog.solve.primitive.Solve

interface SolutionQueue {

    val isOver: Boolean

    /** Returns the head element received from the server.
     * @throws IllegalStateException if the stream is already over
     */
    fun popElement(): Solve.Response

}