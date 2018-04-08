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

public class ClassificationConfigUI extends ConfigUI {

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

        iterationsTextArea     = new TextField();
        intervalTextArea       = new TextField();
        continuousRunBtn       = new RadioButton();
        setConfigButton        = new Button("Ok");

        iterationsTextArea.setMaxWidth(30);
        iterationsTextArea.setMaxHeight(10);

        intervalTextArea.setMaxWidth(30);
        intervalTextArea.setMaxHeight(10);

        iterationsPane.setPadding(new Insets(5));
        intervalPane.setPadding(new Insets(5));
        continuousPane.setPadding(new Insets(5));

        iterationsPane.getChildren().addAll(maxIterationsLbl, iterationsTextArea);
        intervalPane.getChildren().addAll(intervalLbl, intervalTextArea);
        continuousPane.getChildren().addAll(continuousRunLbl, continuousRunBtn);

        mainPane.getChildren().addAll(iterationsPane, intervalPane, continuousPane, setConfigButton);

        setButtonActions();
    }

    @Override
    void setButtonActions() {
        continuousRunBtn.setOnAction(event -> { setContinuousRunBtnActions(); });
        setConfigButton.setOnAction(event ->  { setConfigBtnActions();        });
    }

    private void setContinuousRunBtnActions() {
        //TODO in future hw
    }

    private void setConfigBtnActions() {
        //TODO in future hw
    }

}
