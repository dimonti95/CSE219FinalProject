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
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

public class ClassificationConfigUI extends ConfigUI {

    ApplicationTemplate applicationTemplate;

    public ClassificationConfigUI(ApplicationTemplate applicationTemplate){
        this.applicationTemplate = applicationTemplate;
        this.configurationIsSet  = new SimpleBooleanProperty(false);
        this.maxIterations       = 0;
        this.updateInterval      = 0;
        this.continuousRun       = false;
    }

    @Override
    public void show() {
        if(configurationIsSet.get()){
            iterationsField.setText(String.valueOf(maxIterations));
            intervalField.setText(String.valueOf(updateInterval));
            if(continuousRun) { continuousRunBtn.fire(); }
        }
        configWindow.show();
    }

    @Override
    public void init(Stage owner) {
        PropertyManager manager = applicationTemplate.manager;
        Label secondLabel = new Label(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION_RUN_CONFIG_LABEL.name()));

        VBox mainPane = new VBox(10);
        mainPane.setPadding(new Insets(15));
        mainPane.setAlignment(Pos.BASELINE_CENTER);
        mainPane.getChildren().add(secondLabel);

        Scene secondScene = new Scene(mainPane, 230, 100);

        configWindow =  new Stage();
        configWindow.setTitle(manager.getPropertyValue(AppPropertyTypes.RUN_CONFIG_WINDOW_LABEL.name()));
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

        Label maxIterationsLbl = new Label(manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS_LABEL.name()));
        Label intervalLbl      = new Label(manager.getPropertyValue(AppPropertyTypes.UPDATE_INTERVAL_LABEL.name()));
        Label continuousRunLbl = new Label(manager.getPropertyValue(AppPropertyTypes.CONTINUOUS_RUN_LABEL.name()));

        iterationsField        = new TextField();
        intervalField          = new TextField();
        continuousRunBtn       = new RadioButton();
        setConfigButton        = new Button(manager.getPropertyValue(AppPropertyTypes.SAVE_BUTTON.name()));

        iterationsField.setMaxWidth(35);
        iterationsField.setMaxHeight(10);

        intervalField.setMaxWidth(35);
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
            setConfigIsSetTrue();
            configWindow.close();
        } else { setConfigIsSetFalse(); }
    }

    /* called from setConfigBtnActions() */
    private boolean getContinuousRunValue() {
        if(continuousRunBtn.isSelected()){ return true; }
        else                             { return false; }
    }

    /* called from setConfigBtnActions() */
    private boolean allFieldsValid(){
        if(iterationsField.getText().isEmpty()) { iterationsField.setText("10"); }
        if(intervalField.getText().isEmpty())   { intervalField.setText("5");    }
        try {
            if(Integer.parseInt(iterationsField.getText()) <= 0) { iterationsField.setText("1"); }
            if(Integer.parseInt(intervalField.getText()) <= 0)   { intervalField.setText("1");
                                                                 return true; }
        } catch (Exception e) { setDefaultFeildValues();
                                return true; }
        return true;
    }

    private void setConfigIsSetTrue(){
        configurationIsSet.set(true);
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
    }

    private void setConfigIsSetFalse(){
        configurationIsSet.set(false);
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
    }

    private void setDefaultFeildValues(){
        iterationsField.setText("10");
        intervalField.setText("5");
        continuousRun = false;
    }


}
