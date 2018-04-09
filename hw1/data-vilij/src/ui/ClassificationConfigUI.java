package ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

public class ClassificationConfigUI extends ConfigUI {

    ApplicationTemplate applicationTemplate;

    public ClassificationConfigUI(ApplicationTemplate applicationTemplate){
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void show() {
        configWindow.show();
    }

    @Override
    public void init(Stage owner) {
        /* initializing configuration values */
        configurationIsSet = new SimpleBooleanProperty(false);
        maxIterations      = 0;
        updateInterval     = 0;
        continuousRun      = false;

        Label secondLabel = new Label("Classification Run Configuration");

        VBox mainPane = new VBox(10);
        mainPane.setPadding(new Insets(15));
        mainPane.setAlignment(Pos.BASELINE_CENTER);
        mainPane.getChildren().add(secondLabel);

        Scene secondScene = new Scene(mainPane, 230, 100);

        configWindow =  new Stage();
        configWindow.setTitle("Run Configuration");
        configWindow.setScene(secondScene);

        /* Specifies the modality for new window */
        configWindow.initModality(Modality.WINDOW_MODAL);

        /* Specifies the owner Window (parent) for new window */
        configWindow.initOwner(owner);

        configWindow.setWidth(300);
        configWindow.setHeight(250);

        /* Specifies UI layout */
        HBox  iterationsPane   = new HBox();
        HBox  intervalPane     = new HBox();
        HBox  continuousPane   = new HBox();

        Label maxIterationsLbl = new Label("Max. Iterations: ");
        Label intervalLbl      = new Label("Update Interval: ");
        Label continuousRunLbl = new Label("Continuous Run? ");

        iterationsField        = new TextField();
        intervalField          = new TextField();
        continuousRunBtn       = new RadioButton();
        setConfigButton        = new Button("Ok");

        iterationsField.setMaxWidth(30);
        iterationsField.setMaxHeight(10);

        intervalField.setMaxWidth(30);
        intervalField.setMaxHeight(10);

        iterationsPane.setPadding(new Insets(5));
        intervalPane.setPadding(new Insets(5));
        continuousPane.setPadding(new Insets(5));

        iterationsPane.getChildren().addAll(maxIterationsLbl, iterationsField);
        intervalPane.getChildren().addAll(intervalLbl, intervalField);
        continuousPane.getChildren().addAll(continuousRunLbl, continuousRunBtn);

        mainPane.getChildren().addAll(iterationsPane, intervalPane, continuousPane, setConfigButton);

        setButtonActions();
    }

    @Override
    void setButtonActions() {
        setConfigButton.setOnAction(event ->  { setConfigBtnActions(); });
    }

    private void setConfigBtnActions() {
        if(allFieldsValid()) {
            maxIterations  = Integer.parseInt(iterationsField.getText());
            updateInterval = Integer.parseInt(intervalField.getText());
            continuousRun  = getContinuousRunValue();
        }
    }

    /* called from setConfigBtnActions() */
    private boolean getContinuousRunValue() {
        if(continuousRunBtn.isSelected()){ return true; }
        else                             { return false; }
    }

    /* called from setConfigBtnActions() */
    private boolean allFieldsValid(){
        if(iterationsField.getText().isEmpty() || intervalField.getText().isEmpty()) {
            ((AppUI)applicationTemplate.getUIComponent()).emptyFieldError(); return false; }
        try {
            if(Integer.parseInt(iterationsField.getText()) < 0) {
                ((AppUI)applicationTemplate.getUIComponent()).inputError(); return false; }

            if(Integer.parseInt(intervalField.getText()) < 0) {
                ((AppUI)applicationTemplate.getUIComponent()).inputError(); return false; }
        } catch (Exception e) { ((AppUI)applicationTemplate.getUIComponent()).inputError(); return false; }
        return true;
    }

}
