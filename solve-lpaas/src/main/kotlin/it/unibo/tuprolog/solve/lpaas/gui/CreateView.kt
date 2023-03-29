package it.unibo.tuprolog.solve.lpaas.gui

import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text

class CreateView {
    private var listener: ((Theory) -> Unit)? = null

    private var onClose: (() -> Unit)? = null

    @FXML
    lateinit var txtStaticKb: TextArea

    @FXML
    lateinit var btnCreate: Button

    @FXML
    lateinit var btnCancel: Button

    @FXML
    lateinit var pgrConnection: ProgressIndicator

    @FXML
    lateinit var txtError: Text

    @FXML
    lateinit var root: BorderPane


    @FXML
    fun onCreatePressed() {
        Platform.runLater {
            txtError.isVisible = false
            pgrConnection.isVisible = true
            try {
                val theory = Theory.parse(txtStaticKb.text)
                listener!!(theory)
                this.onClose!!()
            } catch(e: Exception) {
                println(e)
                pgrConnection.isVisible = false
                txtError.text = "Theory not valid"
                txtError.isVisible = true
            }
        }
    }

    @FXML
    fun onCancelPressed() {
        this.onClose!!()
    }

    fun setListener(listener: (Theory) -> Unit) {
        this.listener = listener
    }

    fun setOnClose(onClose: () -> Unit) {
        this.onClose = onClose
    }
}
