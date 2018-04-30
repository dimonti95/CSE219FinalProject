package ui;

import actions.AppActions;

import algorithms.DataSet;
import algorithms.RandomClassifier;
import algorithms.RandomClusterer;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.IOException;
import java.util.*;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    /** Workspace Panes */
    private VBox                         leftPanel;      // left panel of the workspace
    private HBox                         processButtonsBox;

    /** Data Duplicate Information */
    public  String                       duplicate;      // duplicate instance (if found)
    public  boolean                      duplicateFound; // whether or not a duplicate instance was found

    /** Algorithm Type UI */
    private VBox                         algTypeOptionPane;
    private Label                        dataInfo;                 // the label used to display data statistics
    private boolean                      classificationIsSelected; // boolean that indicates to run the classification alg
    private boolean                      clusteringIsSelected;     // boolean that indicates to run the clustering alg

    /** Classification Algorithm UI */
    private VBox                         classificationAlgOptionPane;
    private RadioButton                  randomClassificationBtn;

    /** Clustering Algorithm UI */
    private VBox                         clusteringAlgOptionPane;
    private RadioButton                  randomClusteringBtn;

    /** Algorithms */
    private RandomClassifier             randomClassifier;
    private Thread                       algorithmThread;

    /** Edit/Done UI */
    private ToggleButton                 doneDataToggle;

    /** Algoirthm Configuration Objects */
    private ClassificationConfigUI       classificationConfigUI;
    private ClusteringConfigUI           clusteringConfigUI;

    /** Run Button */
    private VBox                         runButtonPane;
    private Button                       runButton;

    /** getters */
    public LineChart<Number, Number> getChart()          { return chart; }
    public Button                    getNewButton()      { return newButton; }
    public Button                    getLoadButton()     { return loadButton; }
    public Button                    getScrnshotButton() { return scrnshotButton; }
    public Button                    getSaveButton()     { return saveButton; }
    public Button                    getRunButton()      { return runButton; }
    public TextArea                  getTextArea()       { return textArea; }
    public Thread                    getAlgorithmThread(){ return algorithmThread; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        cssPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(PropertyTypes.CSS_RESOURCE_PATH.name()),
                manager.getPropertyValue(AppPropertyTypes.CSS_RESOURCE_FILENAME.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                                                   manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                                   manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        toolBar.getItems().add(scrnshotButton);
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
        newButton.setDisable(false);
        hasNewText = true;
        ((AppActions) applicationTemplate.getActionComponent()).setWasLoadedProperty(false);
        layout();
        setWorkspaceActions();
        runButton = new Button(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.RUN_BUTTON.name()));
        classificationConfigUI = new ClassificationConfigUI(applicationTemplate);
        clusteringConfigUI     = new ClusteringConfigUI(applicationTemplate);
        initializeAlgorithmTypeOptions();
    }

    @Override
    public void clear() {
        ((AppActions) applicationTemplate.getActionComponent()).setWasLoadedProperty(false);
        chart.getData().clear();
    }

    public String getCurrentText() { return textArea.getText(); }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        applicationTemplate.getUIComponent().getPrimaryScene().getStylesheets().add(cssPath);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setAnimated(false);

        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        //leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3); //original specs
        //leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3); //original specs
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 1.0);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 1.0);

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        textArea.setVisible(false);

        processButtonsBox = new HBox();
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.setSpacing(10);

        dataInfo = new Label("");
        dataInfo.setPadding(new Insets(5));

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox, dataInfo);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setScrnshotButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {

            AppActions actionComponent = (AppActions) applicationTemplate.getActionComponent();
            actionComponent.setIsUnsavedProperty(true);

            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                        hasNewText = true;
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void displayToChart(){
        try {
            chart.getData().clear();
            setDisplayActions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setScrnshotButtonActions() {
        scrnshotButton.setOnAction(event -> {
            try{ ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest(); }
            catch (IOException ex) { ((AppActions) applicationTemplate.getActionComponent()).saveErrHandlingHelper(); }
        });
    }

    /* called from loadData(String dataString)*/
    public void checkForDuplicates(){
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        ArrayList<String> pointNames = dataComponent.getTSDProcessor().pointNames;
        Set<String> set = new HashSet<>();
        set.clear();

        for(String pointName : pointNames){
            if(!set.add(pointName))    { duplicate = pointName; duplicateFound = true; break; }
            else                       { duplicateFound = false; }
        }
    }

    private void setChartToolTips() {
        ArrayList<String> orderedPointNames = ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().orderedPointNames;
        int totalSeries     = chart.getData().size();
        int totalSeriesPoints;
        int series;
        int points;
        int iterator = 0;

        for(series = 0; series < totalSeries; series++){
            totalSeriesPoints = chart.getData().get(series).getData().size();

            for(points = 0; points < totalSeriesPoints; points++){
                try {

                    Node chartSymbol = chart.getData().get(series).getData().get(points).getNode();
                    Tooltip tooltip = new Tooltip(orderedPointNames.get(iterator++));
                    Tooltip.install(chartSymbol, tooltip);
                    chartSymbol.setCursor(Cursor.HAND);

                } catch (IndexOutOfBoundsException e){}
            }
        }
    }

    public void setDisplayActions() {
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        if(!duplicateFound) {
            dataComponent.displayData();
            setChartToolTips();
        }
    }

    /** New Data and Load Data UI actions */
    /* called from new data Button */
    public void showNewDataUI(){
        clearMainWindow();
        showTextArea();
        showDoneOption();
        scrnshotButton.setDisable(true);
    }

    /* called from load data Button */
    public void setLoadedDataUI(){
        hideDoneOption();
        showTextArea();
        textArea.setDisable(true);
        scrnshotButton.setDisable(true);
    }

    public void clearMainWindow(){
        hideTextArea();
        hideDoneOption();
        dataInfo.setText("");
        hideAlgorithmTypeOption();
        hideClassificationAlgorithmOption();
        hideClusteringAlgorithmOption();
        hideRunButton();
    }


    /** TextArea UI methods */
    public void showTextArea(){
        textArea.clear();
        textArea.setDisable(false);
        textArea.setVisible(true);
    }

    private void hideTextArea(){
        textArea.setVisible(false);
    }

    /** Data Information Generation Actions(ie: number of instances, total distinct labels, file/path name) */
    /* called from load data button and setDoneOptionActions() */
    public void generateDataInformation() {
        PropertyManager manager = applicationTemplate.manager;
        AppData    dataComponent       = (AppData) applicationTemplate.getDataComponent();
        AppActions actionComponent     = (AppActions) applicationTemplate.getActionComponent();
        String     instanceMessage     = manager.getPropertyValue(AppPropertyTypes.INSTANCES_MESSAGE.name());
        String     pathMessage         = manager.getPropertyValue(AppPropertyTypes.PATH_MESSAGE.name());
        String     labelMessage        = manager.getPropertyValue(AppPropertyTypes.LABEL_MESSAGE.name());

        Integer    numOfInstances      = dataComponent.getTSDProcessor().numOfInstances;
        Integer    numOfDistinctLabels = dataComponent.getTSDProcessor().numOfDistinctLabels;
        String     fileName            = actionComponent.getFileName();
        String     labelsList          = generateLabelsList();
        String     DataInformation     = numOfInstances + instanceMessage + numOfDistinctLabels +
                pathMessage + System.getProperty("line.separator") + fileName +  System.getProperty("line.separator") +
                labelMessage + System.getProperty("line.separator") + labelsList;

        dataInfo.setText(DataInformation);
        showAlgorithmTypeOption();
    }

            /* called from generateDataInformation() */
            private String generateLabelsList() {
                AppData             dataComponent   = (AppData) applicationTemplate.getDataComponent();
                LinkedList<String>  distinctLabels  = dataComponent.getTSDProcessor().distinctLabels;
                StringBuilder       labelsList      = new StringBuilder();
                distinctLabels.forEach((label) -> {
                    labelsList.append("- " + label + System.getProperty("line.separator"));
                });
                return labelsList.toString();
            }

    /** Algorithm Type Option UI and Actions */
    /* called from generateDataInformation() */
    private void showAlgorithmTypeOption() {
        PropertyManager manager = applicationTemplate.manager;
        hideAlgorithmTypeOption();
        hideRunButton();
        hideClassificationAlgorithmOption();
        hideClusteringAlgorithmOption();

        algTypeOptionPane = new VBox();

        AppData dataComponent = ((AppData) applicationTemplate.getDataComponent());
        Button classificationTypeBtn = new Button(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_BUTTON.name()));
        Button clusteringTypeBtn     = new Button(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_BUTTON.name()));
        Label  algorithmTypeLbl      = new Label(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_TYPE_LABEL.name()));
        Integer numOfDistinctLabels = dataComponent.getTSDProcessor().numOfDistinctLabels;

        classificationTypeBtn.setMinWidth(100);
        clusteringTypeBtn.setMinWidth(100);

        classificationTypeBtn.setOnAction(event -> { setClassificationBtnActions(); });
        clusteringTypeBtn.setOnAction    (event -> { setClusteringBtnActions();     });

        if (numOfDistinctLabels != 2) { classificationTypeBtn.setDisable(true); }

        algTypeOptionPane.getChildren().addAll(algorithmTypeLbl, classificationTypeBtn, clusteringTypeBtn);
        algTypeOptionPane.setAlignment(Pos.BASELINE_LEFT);
        leftPanel.getChildren().add(algTypeOptionPane);
    }

            private void setClassificationBtnActions() {
                hideAlgorithmTypeOption();
                showClassificationAlgorithmOption();
                setClassificationIsSelected(); //updating boolean values
            }

            private void setClusteringBtnActions() {
                hideAlgorithmTypeOption();
                showClusteringAlgorithmOption();
                setClusteringIsSelected(); //updating boolean values
            }

    /* called from new data button */
    private void hideAlgorithmTypeOption() {
        leftPanel.getChildren().remove(algTypeOptionPane);
    }

    /** Done/Edit Button UI and Actions */
    /* called from new data */
    private void showDoneOption() {
        PropertyManager manager = applicationTemplate.manager;
        hideDoneOption();
        doneDataToggle = new ToggleButton(manager.getPropertyValue(AppPropertyTypes.DONE_BUTTON.name()));
        doneDataToggle.setMinWidth(100);
        processButtonsBox.getChildren().add(doneDataToggle);

        doneDataToggle.setOnAction(event -> {
            if(textArea.getText().isEmpty()) { doneDataToggle.setSelected(false); }
            else {
                if (doneDataToggle.isSelected()) {
                    doneDataToggle.setText(manager.getPropertyValue(AppPropertyTypes.EDIT_BUTTON.name()));
                    textArea.setDisable(true);
                    setDoneOptionActions();
                } else {
                    doneDataToggle.setText(manager.getPropertyValue(AppPropertyTypes.DONE_BUTTON.name()));
                    textArea.setDisable(false);
                    hideAlgorithmTypeOption();
                    hideClassificationAlgorithmOption();
                    hideClusteringAlgorithmOption();
                    hideRunButton();
                    dataInfo.setText("");
                }
            }
        });

    }

            /* called from showDoneOption -> dataDataToggle.setOnAction() */
            private void setDoneOptionActions() {
                PropertyManager manager = applicationTemplate.manager;
                String textAreaLoadName = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA_LOADNAME.name());
                ((AppActions) applicationTemplate.getActionComponent()).setLoadedFileName(textAreaLoadName);
                ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());

                boolean dataIsValid = ((AppData) applicationTemplate.getDataComponent()).getDataIsValid();

                if   (duplicateFound) { doneDataToggle.fire(); }
                else if(!dataIsValid) { doneDataToggle.fire(); }
            }

    /* called from showDoneOption and load data button */
    private void hideDoneOption() {
        processButtonsBox.getChildren().remove(doneDataToggle);
    }

    /** Classification and Clustering Algorithm Option UI */
    /* called from setClassificationBtnActions() */
    private void showClassificationAlgorithmOption() {
        PropertyManager manager = applicationTemplate.manager;
        Label classificationAlgorithmLbl = new Label(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_LABEL.name()));
        classificationAlgOptionPane = new VBox();
        randomClassificationBtn     = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_RADIOBUTTON.name()));

        randomClassificationBtn.setMinHeight(35);

        Button classificationConfigBtn = setConfigurationButton();

        HBox algorithmOption = new HBox();
        algorithmOption.setSpacing(10);
        algorithmOption.setPadding(new Insets(10));
        algorithmOption.getChildren().addAll(randomClassificationBtn, classificationConfigBtn);

        randomClassificationBtn.setOnAction(event -> { setRandomClassificationBtnActions(); });
        classificationConfigBtn.setOnAction(event -> { showClassificationConfigUI();        });

        classificationAlgOptionPane.getChildren().addAll(classificationAlgorithmLbl, algorithmOption);
        classificationAlgOptionPane.setAlignment(Pos.BASELINE_LEFT);

        leftPanel.getChildren().addAll(classificationAlgOptionPane);
    }
            private void setRandomClassificationBtnActions(){
                if(randomClassificationBtn.isSelected()) { showRunButton(); }
                else { hideRunButton(); }
                if(!classificationConfigUI.configurationIsSet.get()) { runButton.setDisable(true); }
            }

    private void hideClassificationAlgorithmOption() {
        leftPanel.getChildren().remove(classificationAlgOptionPane);
    }

    private void showClusteringAlgorithmOption() {
        PropertyManager manager = applicationTemplate.manager;
        Label clusteringAlgorithmLbl  = new Label(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_LABEL.name()));
        clusteringAlgOptionPane = new VBox();
        randomClusteringBtn     = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLUSTERING_RADIOBUTTON.name()));

        randomClusteringBtn.setMinHeight(35);

        Button clusteringConfigBtn = setConfigurationButton();

        HBox algorithmOption = new HBox();
        algorithmOption.setSpacing(10);
        algorithmOption.setPadding(new Insets(10));
        algorithmOption.getChildren().addAll(randomClusteringBtn, clusteringConfigBtn);

        randomClusteringBtn.setOnAction(event -> { setRandomClusteringBtnActions(); });
        clusteringConfigBtn.setOnAction(event -> { showClusteringConfigUI();        });

        clusteringAlgOptionPane.getChildren().addAll(clusteringAlgorithmLbl, algorithmOption);
        clusteringAlgOptionPane.setAlignment(Pos.BASELINE_LEFT);

        leftPanel.getChildren().add(clusteringAlgOptionPane);
    }
            private void setRandomClusteringBtnActions(){
                if(randomClusteringBtn.isSelected()) { showRunButton(); }
                else { hideRunButton(); }
                if(!clusteringConfigUI.configurationIsSet.get()) { runButton.setDisable(true); }
            }

    private void hideClusteringAlgorithmOption() {
        leftPanel.getChildren().remove(clusteringAlgOptionPane);
    }

    /* called from showClassificationAlgorithmOption() and showClusteringAlgorithmOption() */
    private Button setConfigurationButton(){
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String configurationPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.CONFIGURATION_ICON.name()));
        return new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configurationPath))));
    }

    /** Show Run Configuration UI Actions */
    private void showClassificationConfigUI(){
        classificationConfigUI.init(applicationTemplate.getUIComponent().getPrimaryWindow());
        classificationConfigUI.show();
    }

    private void showClusteringConfigUI(){
        clusteringConfigUI.init(applicationTemplate.getUIComponent().getPrimaryWindow());
        clusteringConfigUI.show();
    }

    /** Run Button Actions */
    private void showRunButton(){
        runButtonPane = new VBox();

        runButtonPane.getChildren().add(runButton);
        runButtonPane.setAlignment(Pos.BOTTOM_LEFT);

        if(classificationIsSelected) {
            runButton.setOnAction(event -> setRunClassificationActions());
        }

        if(clusteringIsSelected) {
            runButton.setOnAction(event -> setRunClusteringActions());
        }

        leftPanel.getChildren().add(runButtonPane);
    }

    private void setRunClassificationActions() {
        DataSet dataSet        = new DataSet();
        int     maxIterations  = classificationConfigUI.maxIterations;
        int     updateInterval = classificationConfigUI.updateInterval;
        boolean continuousRun  = classificationConfigUI.continuousRun;

        randomClassifier = new RandomClassifier
                (dataSet, maxIterations, updateInterval, continuousRun, applicationTemplate);

        algorithmThread = new Thread(randomClassifier); // starting the task in another thread
        runButton.setDisable(true);
        displayToChart();
        algorithmThread.start();
    }

    private void setRunClusteringActions() {
        DataSet dataSet        = ((AppActions) applicationTemplate.getActionComponent()).getLoadedDataSet();
        int     maxIterations  = clusteringConfigUI.maxIterations;
        int     updateInterval = clusteringConfigUI.updateInterval;
        int     numOfLabels    = clusteringConfigUI.totalDistinctLabels;
        boolean continuousRun  = clusteringConfigUI.continuousRun;

        RandomClusterer randomClusterer = new RandomClusterer(dataSet,
                                                              maxIterations,
                                                              updateInterval,
                                                              numOfLabels,
                                                              continuousRun,
                                                              applicationTemplate);

        algorithmThread = new Thread(randomClusterer);
        runButton.setDisable(true);
        displayToChart();
        algorithmThread.start();
    }

    private void hideRunButton(){
        leftPanel.getChildren().remove(runButtonPane);
    }

    /** Selected Algorithm Type Values */
    private void initializeAlgorithmTypeOptions(){
        classificationIsSelected = false;
        clusteringIsSelected     = false;
    }

    private void setClassificationIsSelected(){
        classificationIsSelected = true;
        clusteringIsSelected     = false;
    }

    private void setClusteringIsSelected(){
        classificationIsSelected = false;
        clusteringIsSelected     = true;
    }

    public void displayIntervalIteration(int xCoefficient, int yCoefficient, int constant){
        AppData         dataComponent = ((AppData) applicationTemplate.getDataComponent());
        PropertyManager manager       = applicationTemplate.manager;

        if(yCoefficient == 0) { yCoefficient = 1; } // to avoid dividing by zero

        int     y1         = (constant-xCoefficient*generateXMin()) / yCoefficient;
        int     y2         = (constant-xCoefficient*generateXMax()) / yCoefficient;

        chart.getData().clear();
        dataComponent.displayData();

        XYChart.Series<Number, Number> randomSeries   = new XYChart.Series<>();
        XYChart.Data algorithmPoint1 = new XYChart.Data<>(generateXMin(), y1);
        XYChart.Data algorithmPoint2 = new XYChart.Data<>(generateXMax(), y2);
        randomSeries.getData().addAll(algorithmPoint1, algorithmPoint2);
        randomSeries.setName(manager.getPropertyValue(AppPropertyTypes.CLASSIFIER_SERIES.name()));
        chart.getData().add(randomSeries);
        randomSeries.getNode().setStyle("-fx-stroke-width: 2px");
        randomSeries.getData().get(0).getNode().setStyle("-fx-background-color: transparent, transparent;");
        randomSeries.getData().get(1).getNode().setStyle("-fx-background-color: transparent, transparent;");
    }

    private int generateXMax(){
        AppData              dataComponent = ((AppData) applicationTemplate.getDataComponent());
        Map<String, Point2D> dataPoints    = dataComponent.getTSDProcessor().getDataPoints();
        int                  max           = Integer.MIN_VALUE;
        for (Map.Entry<String, Point2D> entry : dataPoints.entrySet()) {
            if(entry.getValue().getX() > max) { max = (int) entry.getValue().getX(); }
        }
        return max;
    }

    private int generateXMin(){
        AppData              dataComponent = ((AppData) applicationTemplate.getDataComponent());
        Map<String, Point2D> dataPoints    = dataComponent.getTSDProcessor().getDataPoints();
        int                  min           = Integer.MAX_VALUE;
        for (Map.Entry<String, Point2D> entry : dataPoints.entrySet()) {
            if(entry.getValue().getX() < min) { min = (int) entry.getValue().getX(); }
        }
        return min;
    }

    /** Shows the 'Next Interval' Button */
    public void showIntervalButton(){
        PropertyManager manager = applicationTemplate.manager;
        runButtonPane.getChildren().remove(runButton);

        Button nextIntervalButton = new Button(manager.getPropertyValue(AppPropertyTypes.NEXT_INTERVAL_BUTTON.name()));

        runButtonPane.getChildren().add(nextIntervalButton);

        nextIntervalButton.setOnAction(event -> randomClassifier.notifyThread());
    }

    public void disableToolbar(){
        newButton.setDisable(true);
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        printButton.setDisable(true);
        scrnshotButton.setDisable(true);
    }

    public void enableToolbar(){
        newButton.setDisable(false);
        loadButton.setDisable(false);
        scrnshotButton.setDisable(false);
        randomClassificationBtn.setDisable(true);  // required for 'Next Interval' Button functionality
    }




}

