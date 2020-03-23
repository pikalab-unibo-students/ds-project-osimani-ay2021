package it.unibo.tuprolog.solve

import it.unibo.tuprolog.solve.libraries.Libraries
import it.unibo.tuprolog.theory.ClauseDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [StreamsSolver]
 *
 * @author Enrico
 */
internal class StreamsSolverTest {

    @Test
    fun defaultConstructorParameters() {
        val toBeTested = StreamsSolver()

        assertEquals(Libraries(), toBeTested.libraries)
        assertEquals(emptyMap(), toBeTested.flags)
        assertEquals(ClauseDatabase.empty(), toBeTested.staticKB)
        assertEquals(ClauseDatabase.empty(), toBeTested.dynamicKB)
    }

}
