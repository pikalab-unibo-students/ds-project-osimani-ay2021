package ml_Lib

import PythonPrimitivesTestSuite
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.dsl.logicProgramming
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class HousingDatasetTest: PythonPrimitivesTestSuite() {

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
        return logicProgramming {
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

    override fun beforeEach() {
        super.beforeEach()
        logicProgramming {
            solver.appendStaticKb(Theory.Companion.of(
                rule {
                    "createModel"("NInput", "NOutput", G) `if` (
                        "input_layer"("NInput", A) and
                            "dense_layer"(A, 100, "relu", B) and
                            "dense_layer"(B, 50, "relu", C) and
                            "output_layer"(C, "NOutput", "relu", F) and
                            "neural_network"(F, G)
                        )
                },
                rule {
                    "getDataset"(X) `if` (
                        "theory_to_dataset"(schemaName, X)
                        )
                },
                /* Trains a NN multiple times, over Dataset, using the provided Params. */
                /* Returns the AveragePerformance over a 10-fold CV. */
                rule {
                    "train_cv"("Dataset", "LearnParams", "AllPerformances") `if` (
                        "findall"(
                            "Performance",
                            "train_cv_fold"("Dataset", 10, "LearnParams", "Performance"),
                            "AllPerformances"
                        ) /* and
                            "mean"("AllPerformances", "AveragePerformance")*/
                        )
                },
                /* Trains a NN once, for the k-th round of CV. */
                /* Returns the Performance over the k-th validation set. */
                rule {
                    "train_cv_fold"("Dataset", K, "LearnParams", "Performance") `if` (
                        "fold"("Dataset", K, "Train", "Validation") and
                            "train_validate"("Train", "Validation", "LearnParams", "Performance")
                        )
                },
                /* Trains a NN on the provided TrainingSet, using the provided Params, */
                /* and computes its Performance over the provided ValidationSet. */
                rule {
                    "train_validate"("TrainingSet", "ValidationSet", "LearnParams", "Performance") `if` (
                        "createModel"(13, 1, "NN") and
                            "train"("NN","TrainingSet","LearnParams","TrainedNN") and
                            "test"("NN", "ValidationSet", "Performance")
                        )
                },
                //Computes the Performance of the provided NN against the provided ValidationSet
                rule {
                    "test"("NN", "ValidationSet", "Performance") `if` (
                        "predict"("NN", "ValidationSet", "ActualPredictions") and
                            "mse"("ActualPredictions", "ValidationSet", "Performance")
                        )
                },
                //Computes the Performance of the provided NN against the provided ValidationSet
                rule {
                    "preprocessing"("Dataset", "Labels", "Transformed") `if` (
                        "theory_to_schema"("Schema") and
                            "schema_transformation"("Schema", A) and
                            "normalize"(A, "Labels", B) and
                            "fit"(B, "Dataset", C) and
                            "transform"("Dataset", C, "Transformed")
                        )
                }
            ))
        }
    }

    private val schemaName = "housing"
    val path = "/HousingData.csv"

    @Test
    fun testDemo() {
        val csv = readStrictCsv(path)
        println(csv.first().keys.map { Atom.of(it)})
        solver.appendStaticKb(
            fromCSVtoTheory(
                schemaName,
                csv,
                arrayOf(csv.first().keys.last()))
        )
        logicProgramming {
            val performancesVar = Var.of("AllPerformances")
            solver.solve(
                "getDataset"("Dataset") and
                    "preprocessing"("Dataset", csv.first().keys.map { Atom.of(it)}.dropLast(1), "Transformed") and
                    "train_cv"(
                        "Transformed",
                        arrayOf(
                            "max_epoch"(100), "loss"("mse")
                        ),
                        performancesVar)
            ).toList().map {
                println(it)
                assertTrue(it.isYes)
                assertFalse(it.substitution[performancesVar]!!.castToList().isEmptyList)
            }
        }
    }
}