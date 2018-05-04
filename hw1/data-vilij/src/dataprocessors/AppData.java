package dataprocessors;

import actions.AppActions;
import javafx.scene.control.Button;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    private boolean dataIsValid;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {

    }

    public void loadData(String dataString) {
        AppActions actionComponent = ((AppActions) applicationTemplate.getActionComponent());
        AppUI      uiComponent     = ((AppUI) applicationTemplate.getUIComponent());
        Button     scrnshotButton  = ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton();
        try {
            processor.processString(dataString);
            ((AppUI) applicationTemplate.getUIComponent()).checkForDuplicates();
            if(uiComponent.duplicateFound) { uiComponent.clear();
                                             actionComponent.duplicateHandlingHelper(); }
            else {
                processor.generateLabelInfo();
                setDataIsValid(true);
                ((AppUI) applicationTemplate.getUIComponent()).generateDataInformation();
            }
        } catch (Exception e) {
            scrnshotButton.setDisable(true);
            uiComponent.clear();
            errorHandlingHelper();
            setDataIsValid(false);
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() { processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart()); }

    public boolean getDataIsValid(){ return dataIsValid; }

    public void setDataIsValid(boolean isValid){ dataIsValid = isValid; }

    public TSDProcessor getTSDProcessor(){ return processor; }

    private void errorHandlingHelper() {
        clear();
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        String          newLine  = "\n\n";
        String          lineMsg  = manager.getPropertyValue(AppPropertyTypes.LINE_OF_ERROR.name());
        AtomicInteger   errLine  = ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().lineOfError;
        dialog.show(errTitle, errMsg + errInput + newLine + lineMsg + errLine);
    }



}
