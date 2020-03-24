package it.unibo.tuprolog.solve.library.stdlib.function

import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.solve.library.stdlib.function.testutils.FunctionUtils
import it.unibo.tuprolog.solve.library.stdlib.function.testutils.FunctionUtils.computeOf
import it.unibo.tuprolog.solve.primitive.Signature
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test class for [BitwiseOr]
 *
 * @author Enrico
 */
internal class BitwiseOrTest {

    @Test
    fun functorNameCorrect() {
        assertEquals(Signature("\\/", 2), BitwiseOr.signature)
    }

    @Test
    fun computationCorrect() {
        assertEquals(
            Integer.of(255),
            BitwiseOr.computeOf(
                Integer.of(125),
                Integer.of(255)
            )
        )
    }

    @Test
    fun rejectedInputs() {
        FunctionUtils.assertRejectsNonIntegerParameters(BitwiseOr)
    }

}