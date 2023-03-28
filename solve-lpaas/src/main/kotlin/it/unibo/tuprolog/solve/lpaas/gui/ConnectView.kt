package it.unibo.tuprolog.solve.lpaas.gui

import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text

class ConnectView {
    private var listener: ((MutableSolver) -> Unit)? = null

    private var onClose: (() -> Unit)? = null

    @FXML
    lateinit var txtSolverId: TextField

    @FXML
    lateinit var btnConnect: Button

    @FXML
    lateinit var btnCancel: Button

    @FXML
    lateinit var pgrConnection: ProgressIndicator

    @FXML
    lateinit var txtError: Text

    @FXML
    lateinit var root: BorderPane


    @FXML
    fun onConnectPressed() {
        Platform.runLater {
            txtError.isVisible = false
            pgrConnection.isVisible = true
            try {
                val result = TrasparentFactory.getOnlineSolver(txtSolverId.text)
                listener!!(result as MutableSolver)
                this.onClose!!()
            } catch(e: Exception) {
                println(e)
                pgrConnection.isVisible = false
                txtError.text = "Solver not found"
                txtError.isVisible = true
            }
        }
    }

    @FXML
    fun onCancelPressed() {
        this.onClose!!()
    }

    fun setListener(listener: (MutableSolver) -> Unit) {
        this.listener = listener
    }

    fun setOnClose(onClose: () -> Unit) {
        this.onClose = onClose
    }
}
