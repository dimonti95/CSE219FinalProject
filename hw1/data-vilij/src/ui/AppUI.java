package ui;

import actions.AppActions;
import com.sun.javafx.geom.Path2D;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

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
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private RadioButton                  readOnlyButton; // sets text area to read only

    public String                       duplicate;      // duplicate instance (if found)
    public boolean                      duplicateFound; // whether or not a duplicate instance was found

    public LineChart<Number, Number> getChart() { return chart; }

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
        hasNewText = true;
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
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

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);

        readOnlyButton = new RadioButton(manager.getPropertyValue(AppPropertyTypes.RADIO_BUTTON_TEXT.name()));
        processButtonsBox.getChildren().add(readOnlyButton);
        processButtonsBox.setSpacing(10);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox);

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
        setDisplayButtonActions();
        setRadioButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {

            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.checkDataFormat(getCurrentText());
            checkForDuplicates();

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

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    checkForDuplicates();
                    dataComponent.displayData();
                    plotAverageYLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setRadioButtonActions() {
        readOnlyButton.setOnAction(event -> {
          if(textArea.isDisabled()){
              textArea.setDisable(false);
          } else { textArea.setDisable(true); }
        });
    }

    private Double calculateAverageYValue(){
        Double yValue;
        Double ySum         = 0d;
        int totalSeries     = chart.getData().size();
        int totalPoints     = 0;
        int totalSeriesPoints;
        int series;
        int points;

        for(series = 0; series < totalSeries; series++){
            totalSeriesPoints = chart.getData().get(series).getData().size();
            totalPoints       = totalSeriesPoints + totalPoints;

            for(points = 0; points < totalSeriesPoints; points++){
                yValue = (Double) chart.getData().get(series).getData().get(points).getYValue();
                ySum   = ySum + yValue;
            }

        }
        //System.out.println(ySum / totalPoints); //test
        return ySum / totalPoints;
    }

    private Double calculateMaxXValue(){
        Double maxXValue    = 0d;
        Double currentValue = 0d;
        int totalSeries     = chart.getData().size();
        int totalPoints     = 0;
        int totalSeriesPoints;
        int series;
        int points;

        for(series = 0; series < totalSeries; series++){
            totalSeriesPoints = chart.getData().get(series).getData().size();
            totalPoints       = totalSeriesPoints + totalPoints;

            for(points = 0; points < totalSeriesPoints; points++){
                currentValue =  (Double) chart.getData().get(series).getData().get(points).getXValue();
                if(currentValue > maxXValue) { maxXValue = currentValue; }
            }
        }
        //System.out.println("Max: " + maxXValue); //test
        return maxXValue;
    }

    private Double calculateMinXValue(){
        Double minXValue    = 0d;
        Double currentValue = 0d;
        int totalSeries     = chart.getData().size();
        int totalPoints     = 0;
        int totalSeriesPoints;
        int series;
        int points;

        for(series = 0; series < totalSeries; series++){
            totalSeriesPoints = chart.getData().get(series).getData().size();
            totalPoints       = totalSeriesPoints + totalPoints;

            for(points = 0; points < totalSeriesPoints; points++){
                currentValue =  (Double) chart.getData().get(series).getData().get(points).getXValue();

                if(currentValue < minXValue) { minXValue = currentValue; }
            }
        }
        //System.out.println("Min: " + minXValue); //test
        return minXValue;
    }

    private void plotAverageYLine(){
        boolean dataIsValid = ((AppData)applicationTemplate.getDataComponent()).getDataIsValid();
        if(!textArea.getText().isEmpty() && dataIsValid) {
            PropertyManager manager = applicationTemplate.manager;
            Double aveYValue = calculateAverageYValue();
            Double maxXValue = calculateMaxXValue();
            Double minXValue = calculateMinXValue();

            XYChart.Series<Number, Number> aveYLineSeries = new XYChart.Series<>();
            aveYLineSeries.getData().add(new XYChart.Data<>(minXValue, aveYValue));
            aveYLineSeries.getData().add(new XYChart.Data<>(maxXValue, aveYValue));
            aveYLineSeries.setName(manager.getPropertyValue(AppPropertyTypes.AVERAGE_SERIES_NAME.name()));
            chart.getData().add(aveYLineSeries);
            aveYLineSeries.getNode().setStyle("-fx-stroke-width: 2px");
            aveYLineSeries.getData().get(0).getNode().setStyle("-fx-background-color: transparent, transparent;");
            aveYLineSeries.getData().get(1).getNode().setStyle("-fx-background-color: transparent, transparent;");
        }
    }

    private void checkForDuplicates(){
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
        ArrayList<String> pointNames = dataComponent.getTSDProcessor().pointNames;
        Set<String> set = new HashSet<>();
        set.clear();

        for (int i = 0; i < pointNames.size(); i++) {
            boolean duplicateExists = set.add(pointNames.get(i)); // returns false if duplicate exists
            if(!duplicateExists)         { duplicate = pointNames.get(i); duplicateFound = true; break; }
            else                         { duplicateFound = false; }
        }

    }


    public Button getScrnshotButton() { return scrnshotButton; }

}
