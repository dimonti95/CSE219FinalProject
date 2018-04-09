package ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


/**
 * Defines the behavior and state of the core actions to be handled by the Run Configuration User Interface.
 *
 * @author Nick DiMonti
 */
public abstract class ConfigUI {

    /** back end components */
    SimpleBooleanProperty configurationIsSet;
    Integer maxIterations;
    Integer updateInterval;
    Boolean continuousRun;

    /** front end components */
    Stage       configWindow;
    TextField   iterationsField;
    TextField   intervalField;
    RadioButton continuousRunBtn;
    Button      setConfigButton;

    abstract void show();

    abstract void init(Stage owner);

    abstract void setButtonActions();

}
