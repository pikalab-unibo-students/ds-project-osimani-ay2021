package testGui

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.format
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.theory.Theory
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import javafx.scene.text.Text
import javafx.stage.Stage
import java.net.URL
import java.time.Duration
import java.util.*
import kotlin.math.pow
import kotlin.math.round
import kotlin.system.exitProcess


@Suppress("UNUSED_PARAMETER", "unused")
class TuPrologIDEController : Initializable {

    private val model = TuPrologIDEModel.of()

    private var onClose = {}

    private var onAbout = {}

    @FXML
    private lateinit var sldTimeout: Slider

    @FXML
    private lateinit var lblTimeout: Label

    @FXML
    private lateinit var root: Parent

    @FXML
    private lateinit var lblStatus: Label

    @FXML
    private lateinit var lblSolverId: TextField

    @FXML
    private lateinit var txfQuery: TextField

    @FXML
    private lateinit var btnNext: Button

    @FXML
    private lateinit var btnNextAll: Button

    @FXML
    private lateinit var btnStop: Button

    @FXML
    private lateinit var btnReset: Button

    @FXML
    private lateinit var btnRefresh: Button

    @FXML
    private lateinit var btnNewFile: MenuItem

    @FXML
    private lateinit var btnSettings: MenuItem

    @FXML
    private lateinit var btnConnectTo: MenuItem

    @FXML
    private lateinit var btnQuit: MenuItem

    @FXML
    private lateinit var btnAbout: MenuItem

    @FXML
    private lateinit var tabsStreams: TabPane

    @FXML
    private lateinit var tabSolutions: Tab

    @FXML
    private lateinit var tabStdin: Tab

    @FXML
    private lateinit var tabStdout: Tab

    @FXML
    private lateinit var tabStderr: Tab

    @FXML
    private lateinit var tabWarnings: Tab

    @FXML
    private lateinit var tabOperators: Tab

    @FXML
    private lateinit var tabFlags: Tab

    @FXML
    private lateinit var tabLibraries: Tab

    @FXML
    private lateinit var tabStaticKb: Tab

    @FXML
    private lateinit var tabDynamicKb: Tab

    @FXML
    private lateinit var tbvOperators: TableView<Operator>

    @FXML
    private lateinit var tbcFunctor: TableColumn<Operator, String>

    @FXML
    private lateinit var tbcPriority: TableColumn<Operator, Int>

    @FXML
    private lateinit var tbvFlags: TableView<Pair<String, Term>>

    @FXML
    private lateinit var tbcKey: TableColumn<Operator, String>

    @FXML
    private lateinit var tbcValue: TableColumn<Operator, Term>

    @FXML
    private lateinit var tbcSpecifier: TableColumn<Operator, Specifier>

    @FXML
    private lateinit var trvLibraries: TreeView<String>

    @FXML
    private lateinit var txaStaticKb: TextArea

    @FXML
    private lateinit var txaDynamicKb: TextArea

    @FXML
    private lateinit var prbResolution: ProgressBar

    @FXML
    private lateinit var txaStdin: TextArea

    @FXML
    private lateinit var txaStdout: TextArea

    @FXML
    private lateinit var txaStderr: TextArea

    @FXML
    private lateinit var lsvSolutions: ListView<Solution>

    @FXML
    private lateinit var lsvWarnings: ListView<Warning>

    private val stage: Stage get() = root.scene.window as Stage

    private inline fun onUiThread(crossinline f: () -> Unit) =
        Platform.runLater {
            f()
        }

    private val streamsTabs: Sequence<Tab>
        get() = tabsStreams.tabs.asSequence()

    @FXML
    override fun initialize(location: URL, resources: ResourceBundle?) {
        model.onResolutionStarted.subscribe(this::onResolutionStarted)
        model.onResolutionOver.subscribe(this::onResolutionOver)
        model.onNewQuery.subscribe(this::onNewQuery)
        model.onQueryOver.subscribe(this::onQueryOver)
        model.onNewSolution.subscribe(this::onNewSolution)
        model.onStdoutPrinted.subscribe(this::onStdoutPrinted)
        model.onStderrPrinted.subscribe(this::onStderrPrinted)
        model.onWarning.subscribe(this::onWarning)
        model.onError.subscribe(this::onError)
        model.onNewSolver.subscribe(this::onNewSolver)
        model.onSolverLoaded.subscribe(this::onSolverLoaded)
        model.onSolverClosed.subscribe(this::onSolverClosed)
        model.onNewSolver.subscribe(this::onNewSolver)
        model.onSolveOptionsChanged.subscribe(this::onSolveOptionsChanged)
        model.onReset.subscribe(this::onReset)
        model.onQuit.subscribe(this::onQuit)

        sldTimeout.valueProperty().addListener { _, _, value -> onTimeoutSliderMoved(value) }

        updateTimeoutView(model.solveOptions.timeout)

        lsvSolutions.setCellFactory { ListCellView { SolutionView.of(it) } }
        lsvWarnings.setCellFactory { ListCellView { Label(it.message) } }

        tbcFunctor.cellValueFactory = PropertyValueFactory(Operator::functor.name)
        tbcSpecifier.cellValueFactory = PropertyValueFactory(Operator::specifier.name)
        tbcPriority.cellValueFactory = PropertyValueFactory(Operator::priority.name)

        tbcKey.cellValueFactory = PropertyValueFactory(Pair<String, Term>::first.name)
        tbcValue.cellValueFactory = PropertyValueFactory(Pair<String, Term>::second.name)

        trvLibraries.root = TreeItem("Loaded libraries:")

        //model.newSolver(Theory.empty())
        btnNext.isDisable = true
        btnNextAll.isDisable = true
        btnReset.isDisable = true
        btnStop.isDisable = true
        btnRefresh.isDisable = true
        lblSolverId.text = "... connect to a Solver ..."

        Platform.runLater {
            streamsTabs.forEach { it.hideNotification() }
        }
    }

    private fun onQuit(e: Unit) {
        exitProcess(0)
    }

    private fun onReset(e: SolverEvent<Unit>) {
        lsvWarnings.items.clear()
        txaStdout.clear()
        txaStderr.clear()
        lsvSolutions.items.clear()
    }

    private fun onTimeoutSliderMoved(value: Number) {
        model.solveOptions = model.solveOptions.setTimeout(
            round(10.0.pow(value.toDouble())).toLong()
        )
    }

    private fun onSolveOptionsChanged(newSolveOptions: SolveOptions) {
        updateTimeoutView(newSolveOptions.timeout)
    }

    private fun updateTimeoutView(timeout: TimeDuration) {
        val duration = Duration.ofMillis(timeout)
        lblTimeout.text = duration.pretty()
    }

    private fun Duration.pretty(): String {
        val milliseconds = toMillisPart()
        val seconds = toSecondsPart()
        val minutes = toMinutesPart()
        val hours = toHoursPart()
        val days = toDaysPart() % 365
        val years = toDaysPart() / 365
        return sequenceOf(
            years.pretty("y"),
            days.pretty("d"),
            hours.pretty("h"),
            minutes.pretty("m"),
            seconds.pretty("s"),
            milliseconds.pretty("ms")
        ).filterNotNull().joinToString()
    }

    private fun Long.pretty(unit: String): String? =
        if (this == 0L) null else "$this$unit"

    private fun Int.pretty(unit: String): String? = toLong().pretty(unit)

    private fun onNewSolver(e: SolverEvent<Unit>) = onUiThread {
        updateContextSensitiveView(e)
    }

    private fun onSolverLoaded(e: SolverEvent<Unit>) = onUiThread {
        btnNext.isDisable = false
        btnNextAll.isDisable = false
        btnReset.isDisable = false
        btnStop.isDisable = false
        btnRefresh.isDisable = false
        updateContextSensitiveView(e)
    }

    private fun onSolverClosed(e: String) = onUiThread {}

    private fun onStdoutPrinted(output: String) = onUiThread {
        txaStdout.text += output
        tabStdout.showNotification()
    }

    private fun Tab.showNotification() {
        if (!isSelected && !text.endsWith("*")) {
            text += "*"
        }
    }

    private fun Tab.hideNotification() {
        if (text.endsWith("*")) {
            text = text.substringBefore('*')
        }
    }

    private var lastEvent: SolverEvent<*>? = null

    private fun Theory.pretty(): String =
        if (isNonEmpty) {
            clauses.joinToString(".\n", postfix = ".") {
                it.format(TermFormatter.prettyExpressions())
            }
        } else {
            ""
        }

    private fun updateContextSensitiveView(event: SolverEvent<*>) {
        if (event.operators != lastEvent?.operators) {
            tbvOperators.items.setAll(event.operators)
            tabOperators.showNotification()
        }
        if (event.flags != lastEvent?.flags) {
            tbvFlags.items.setAll(event.flags.map { it.toPair() })
            tabFlags.showNotification()
        }
        if (event.libraries != lastEvent?.libraries) {
            with(trvLibraries.root) {
                children.setAll(event.libraries.libraries.map { LibraryView(it) })
                if (children.isNotEmpty()) {
                    isExpanded = true
                }
            }
            tabLibraries.showNotification()
        }
        if (lastEvent?.staticKb?.let { event.staticKb.equals(it, useVarCompleteName = false) } == false) {
            txaStaticKb.text = event.staticKb.pretty()
            tabStaticKb.showNotification()
        }
        if (lastEvent?.dynamicKb?.let { event.dynamicKb.equals(it, useVarCompleteName = false) } == false) {
            txaDynamicKb.text = event.dynamicKb.pretty()
            tabDynamicKb.showNotification()
        }

        lblSolverId.text = event.solverId
        lastEvent = event
    }

    private fun updatingContextSensitiveView(event: SolverEvent<*>, action: () -> Unit) {
        action()
        updateContextSensitiveView(event)
    }

    private fun onStderrPrinted(output: String) = onUiThread {
        txaStderr.text += output
        tabStderr.showNotification()
    }

    private fun onWarning(warning: Warning) = onUiThread {
        lsvWarnings.items.add(warning)
        lsvWarnings.scrollTo(warning)
        tabWarnings.showNotification()
    }

    private fun onError(exception: Exception) = onUiThread {
        val dialog = Alert(Alert.AlertType.ERROR)
        dialog.title = exception::class.java.simpleName
        when (exception) {
            is SyntaxException.InTheorySyntaxError -> {
                dialog.headerText = "Syntax error in ${exception.file.name}"
                dialog.dialogPane.content = exception.message.toMonospacedText()
            }
            is SyntaxException.InQuerySyntaxError -> {
                dialog.headerText = "Syntax error in query"
                dialog.dialogPane.content = exception.message.toMonospacedText()
            }
            else -> {
                dialog.headerText = "Error"
                dialog.dialogPane.content = exception.message?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }?.toMonospacedText()
            }
        }
        dialog.dialogPane.minHeight = Region.USE_PREF_SIZE
        dialog.dialogPane.minWidth = Region.USE_PREF_SIZE
        dialog.showAndWait()
    }

    private fun String?.toMonospacedText(): Node {
        if (this == null) return Text()
        return Text(this).also { it.style = "-fx-font-family: monospaced" }
    }

    private fun onNewSolution(e: SolverEvent<Solution>) = onUiThread {
        updatingContextSensitiveView(e) {
            lsvSolutions.items.add(e.event)
            lsvSolutions.scrollTo(e.event)
            tabSolutions.showNotification()
        }
    }

    private fun onNewQuery(e: SolverEvent<Struct>) = onUiThread {
        updatingContextSensitiveView(e) {
            cleanUpAfterResolution()
            btnStop.isDisable = false
            txaStdin.isDisable = true
            btnNext.text = "Next"
            btnNextAll.text = "All next"
        }
    }

    private fun onQueryOver(e: SolverEvent<Struct>) = onUiThread {
        updatingContextSensitiveView(e) {
            lblStatus.text = "Idle"
            btnStop.isDisable = true
            txaStdin.isDisable = false
            btnNext.text = "Solve"
            btnNextAll.text = "Solve all"
        }
    }

    private fun onResolutionStarted(e: SolverEvent<Int>) = onUiThread {
        updatingContextSensitiveView(e) {
            btnNext.isDisable = true
            btnNextAll.isDisable = true
            btnStop.isDisable = true
            btnReset.isDisable = true
            prbResolution.isVisible = true
            txfQuery.isDisable = true
            lblStatus.text = "Solving..."
        }
    }

    private fun onResolutionOver(e: SolverEvent<Int>) = onUiThread {
        updatingContextSensitiveView(e) {
            btnNext.isDisable = false
            btnNextAll.isDisable = false
            prbResolution.isVisible = false
            btnStop.isDisable = false
            btnReset.isDisable = false
            txfQuery.isDisable = false
            lblStatus.text = "Solution ${e.event}"
        }
    }

    @FXML
    fun onKeyTypedOnQuery(e: KeyEvent) {
        model.query = txfQuery.text
    }

    @FXML
    fun onNextButtonPressed(e: ActionEvent) {
        if (model.state == TuPrologIDEModel.State.IDLE) {
            startNewResolution()
        } else {
            continueResolution()
        }
    }

    @FXML
    fun onStdinChanged(e: KeyEvent) {
        model.setStdin(txaStdin.text)
    }

    @FXML
    fun onNextAllButtonPressed(e: ActionEvent) {
        if (model.state == TuPrologIDEModel.State.IDLE) {
            startNewResolution(true)
        } else {
            continueResolution(true)
        }
    }

    @FXML
    fun onStopButtonPressed(e: ActionEvent) {
        model.stop()
    }

    @FXML
    fun onResetButtonPressed(e: ActionEvent) {
        model.reset()
    }

    @FXML
    fun onRefreshButtonPressed(e: ActionEvent) {
        println("hello")
        lsvWarnings.items.clear()
        txaStdout.clear()
        txaStderr.clear()
        lsvSolutions.items.clear()
        updateContextSensitiveView(SolverEvent(Unit, model.getCurrentSolver()!!))
    }

    private fun continueResolution(all: Boolean = false) {
        if (all) {
            model.nextAll()
        } else {
            model.next()
        }
    }

    private fun startNewResolution(all: Boolean = false) {
        if (all) {
            model.solveAll()
        } else {
            model.solve()
        }
    }

    private fun cleanUpAfterResolution() {
        lsvSolutions.items.clear()
    }

    @FXML
    fun onQuitRequested(e: ActionEvent) {
        this.onClose()
    }

    @FXML
    fun onActionPerformedOnQuery(e: ActionEvent) {
        startNewResolution()
    }

    @FXML
    fun onTabSelectionChanged(e: Event) {
        val tab = e.source as Tab
        if (tab.isSelected) {
            tab.hideNotification()
        }
    }

    @FXML
    fun onNewSolverPressed(e: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("NewView.fxml"))
        val root = loader.load<Parent>()
        val stage = Stage()
        stage.title = "Create New Solver"
        stage.scene = Scene(root)
        stage.show()
        val controller = loader.getController() as CreateView
        controller.setListener {theory ->
            model.newSolver(theory)
        }
        controller.setOnClose { stage.close() }

    }

    @FXML
    fun onConnectToSolverPressed(e: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("ConnectView.fxml"))
        val root = loader.load<Parent>()
        val stage = Stage()
        stage.title = "Connect To Existing Solver"
        stage.scene = Scene(root)
        stage.show()
        val controller = loader.getController() as ConnectView
        controller.setListener {solverId ->
            model.loadSolver(solverId)
        }
        controller.setOnClose { stage.close() }
    }

    @FXML
    fun onSettingsPressed(e: ActionEvent) {
    }

    @FXML
    fun onAbout(e: ActionEvent) {
        this.onAbout()
    }

    fun customizeModel(setup: ModelConfigurator) = setup(model)

    /**
     * Adds a [Tab] to the UI. If a [Tab] with the same [Tab.id] as [tab] is
     * already present, it gets substituted.
     */
    fun addTab(tab: Tab) {
        val index = this.tabsStreams.tabs.indexOfFirst { it.id == tab.id }
        if (index >= 0) {
            this.tabsStreams.tabs[index] = tab
        } else {
            this.tabsStreams.tabs.add(tab)
        }
    }

    fun setOnClose(onClose: () -> Unit) {
        this.onClose = onClose
    }

    fun setOnAbout(onAbout: () -> Unit) {
        this.onAbout = onAbout
    }
}
