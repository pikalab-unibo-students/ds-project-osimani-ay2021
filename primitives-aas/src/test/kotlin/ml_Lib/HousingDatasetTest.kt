package ml_Lib

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class HousingDatasetTest {

    private fun readStrictCsv(path: String): List<Map<String, Number>> {
        val reader = javaClass.getResourceAsStream(path)!!.bufferedReader()
        val headers = reader.readLine().split(',')
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                headers.zip(it.split(',')
                    .map { value -> try {
                        value.toBigDecimal()
                    } catch(_: NumberFormatException) {
                        0
                    } }).toMap()
            }.toList()
    }

    private fun fromCSVtoTheory(
        title: String,
        csv: List<Map<String, Number>>,
        targets: Array<String> = emptyArray()
    ): Theory {
        return it.unibo.tuprolog.dsl.logicProgramming {
            val theory = mutableListOf<Clause>(
                fact { "schema_name"(title) },
                fact { "schema_target"(targets.map { Atom.of(it) }) }
            )

            val attributes = mutableListOf<Clause>()
            for (attr in csv.first().keys) {
                attributes.add(
                    fact { "attribute"(attributes.size, Atom.of(attr), "real") }
                )
            }
            theory.addAll(attributes)
            theory.addAll(
                csv.map {
                    fact {
                        Struct.of(
                            title,
                            it.values.map { value -> value.toTerm() }
                        )
                    }
                })
            return@logicProgramming Theory.of(theory)
        }
    }

    private val schemaName = "housing"

    @Test
    @Throws(Exception::class)
    fun testProcessing() {
        val path = "/HousingData.csv"
        val theory = fromCSVtoTheory(
            schemaName,
            readStrictCsv(path),
            arrayOf("3"))
        print(theory.last())
    }
}