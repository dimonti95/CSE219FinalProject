package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    // added
    private Label                        textAreaLabel;  // label above textArea
    private Label                        chartLabel;     // label above chart
    private NumberAxis                   xAxis;          // x axis value
    private NumberAxis                   yAxis;          // y axis value
    //private AppActions actionsController = new AppActions(applicationTemplate); // MAY NOT NEED
    //private AppData dataController = new AppData(applicationTemplate);

    public ScatterChart<Number, Number> getChart() { return chart; }

    /* constructor */
    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        // TODO for homework 1
        super.setToolBar(applicationTemplate);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        initializeChart();
        workspace = new GridPane();
        displayButton = new Button("Display");
        textArea = new TextArea();
        textAreaLabel = new Label("Data File");
        chartLabel = new Label("Data Visualization");
        appPane.getChildren().add(workspace);
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        // TODO for homework 1
        applicationTemplate.getDataComponent().clear(); // TEST
    }

    private void layout() {
        // TODO for homework 1
        workspace.setPadding(new Insets(10));
        textArea.setMaxSize(250, 125);
        setGridPaneConstraints();
        workspace.getChildren().add(textAreaLabel);
        workspace.getChildren().add(textArea);
        workspace.getChildren().add(displayButton);
        workspace.getChildren().add(chartLabel);
        workspace.getChildren().add(chart);
    }

    private void setWorkspaceActions() {
        // TODO for homework 1
        displayButton.setOnAction(e -> {
            try {
                ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
                ((AppData) applicationTemplate.getDataComponent()).displayData();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }



    /* ADDED METHODS */
    private void initializeChart(){
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        chart =  new ScatterChart<Number, Number>(xAxis, yAxis);
    }

    private void setGridPaneConstraints(){
        GridPane.setConstraints(textAreaLabel, 0, 0);
        GridPane.setConstraints(textArea, 0, 1);
        GridPane.setConstraints(displayButton, 0,2);
        GridPane.setConstraints(chartLabel, 1, 0);
        GridPane.setConstraints(chart, 1, 1);
    }



}
