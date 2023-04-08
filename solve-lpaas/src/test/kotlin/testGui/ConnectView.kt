package testGui

import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverFactory
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text

class ConnectView {
    private var listener: ((String) -> Unit)? = null

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
                ClientPrologSolverFactory.connectToSolver(txtSolverId.text)!!.closeClient()
                listener!!(txtSolverId.text)
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

    fun setListener(listener: (String) -> Unit) {
        this.listener = listener
    }

    fun setOnClose(onClose: () -> Unit) {
        this.onClose = onClose
    }
}
