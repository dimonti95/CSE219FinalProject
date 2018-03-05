package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import sun.security.krb5.internal.APOptions;
import ui.AppUI;
import ui.DataVisualizer;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.io.File.separator;
import static settings.AppPropertyTypes.LOAD_WORK_TITLE;
import static settings.AppPropertyTypes.SAVE_SCRNSHOT_TITLE;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    /** Path to the screenshot file currently active. */
    Path scrnshotFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    private ArrayList<String> firstTenLines;
    private int               totalLinesOfData;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                ((AppUI) applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                isUnsaved.set(false);
                dataFilePath = null;
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
        AppData dataComponent  = (AppData) applicationTemplate.getDataComponent();
        boolean duplicateFound = ((AppUI) applicationTemplate.getUIComponent()).duplicateFound;

        if(duplicateFound) { duplicateHandlingHelper(); dataComponent.setDataIsValid(false); }
        if(!dataComponent.getDataIsValid() && !duplicateFound) { errorHandlingHelper(); }

        if(dataComponent.getDataIsValid() && isUnsaved.getValue()) {
            if(dataFilePath != null) {
                try {
                    save();
                } catch (IOException e) {
                    errorHandlingHelper();
                }
            }
            try {
                saveRequestHandler();
                isUnsaved.set(false);
                ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                }
            catch (IOException e) {
                errorHandlingHelper();
            }
        }
    }

    @Override
    public void handleLoadRequest() {
        PropertyManager    manager = applicationTemplate.manager;


        if(dataFilePath == null){ /* no previously saved data */  }

        FileChooser fileChooser = new FileChooser();
        String      dataDirPath = separator + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL         dataDirURL  = getClass().getResource(dataDirPath);

        if (dataDirURL == null) { /* file not found exception */ }

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(manager.getPropertyValue(LOAD_WORK_TITLE.name()));

        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                String.format("*.%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if(selected != null) {

            String data = "";
            int i = 0;
            totalLinesOfData = 0;
            firstTenLines = new ArrayList<>(10);

            try {
                Scanner scanner = new Scanner(selected);
                while (scanner.hasNextLine()) {
                    totalLinesOfData++;
                    String lineOfData = scanner.nextLine();
                    data = data + lineOfData + "\n";
                    if (i < 10) {
                        firstTenLines.add(lineOfData);
                        i++;
                    }
                }
                outputDataToTxtArea();
                ((AppData) applicationTemplate.getDataComponent()).loadData(data);
                ((AppUI)   applicationTemplate.getUIComponent()).setDisplayActions();
                ((AppUI)   applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                ((AppUI)   applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(false);
            } catch (FileNotFoundException e) {
                errorHandlingHelper2();
            }
        }   else { /* no file selected */ }
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        LineChart<Number, Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();

        WritableImage writableImage = new WritableImage((int)chart.getWidth(), (int)chart.getHeight());
        chart.snapshot(null, writableImage);

        if(scrnshotFilePath == null){
            FileChooser fileChooser = new FileChooser();
            String      dataDirPath = separator + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
            URL         dataDirURL  = getClass().getResource(dataDirPath);

            if (dataDirURL == null)
                throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

            fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
            fileChooser.setTitle(manager.getPropertyValue(SAVE_SCRNSHOT_TITLE.name()));

            String description = manager.getPropertyValue(AppPropertyTypes.SCRNSHOT_FILE_EXT_DESC.name());
            String extension   = manager.getPropertyValue(AppPropertyTypes.SCRNSHOT_FILE_EXT.name());
            ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                    String.format("*.%s", extension));

            fileChooser.getExtensionFilters().add(extFilter);
            File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (selected != null) {
                dataFilePath = selected.toPath();

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", selected);

            } else {
                IOException ex
                        = new IOException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
                throw ex;
                }
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = separator + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                                                                String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        String          newLine  = "\n\n";
        String          lineMsg  = manager.getPropertyValue(AppPropertyTypes.LINE_OF_ERROR.name());
        AtomicInteger   errLine  = ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().lineOfError;
        dialog.show(errTitle, errMsg + errInput + newLine + lineMsg + errLine.get());
    }

    public void errorHandlingHelper2() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }

    private void duplicateHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        String          newLine  = "\n\n";
        String          dupeMsg  = manager.getPropertyValue(AppPropertyTypes.DUPLICATE_ERROR.name());
        String          dupe     = ((AppUI) applicationTemplate.getUIComponent()).duplicate;
        dialog.show(errTitle, errMsg + errInput + newLine + dupeMsg + dupe);
    }

    private void outputDataToTxtArea(){
        TextArea textArea = ((AppUI) applicationTemplate.getUIComponent()).getTextArea();
            for (int i = 0; i < firstTenLines.size(); i++) {
                if (firstTenLines.get(i) == null) { break; }
                else                              { textArea.appendText(firstTenLines.get(i) + "\n"); }
            }
        if(totalLinesOfData > 10) { promptUserAboutTotalData(); }
        firstTenLines.clear();
    }

    private void promptUserAboutTotalData() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          loadMsgTitle = manager.getPropertyValue(AppPropertyTypes.TOTAL_DATA_LOADED_TITLE.name());
        String          loadMsg      = manager.getPropertyValue(AppPropertyTypes.TOTAL_DATA_LOADED_MSG.name());
        String          newLine      =  "\n\n";
        String          totalDataMsg = manager.getPropertyValue(AppPropertyTypes.TOTAL_DATA_MSG.name());
        String          lines        = manager.getPropertyValue(AppPropertyTypes.LINES.name());
        dialog.show(loadMsgTitle,  totalDataMsg + totalLinesOfData + lines + newLine + loadMsg);
    }

    private void saveRequestHandler() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        if (dataFilePath == null) {
            FileChooser fileChooser = new FileChooser();
            String      dataDirPath = separator + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
            URL         dataDirURL  = getClass().getResource(dataDirPath);

            if (dataDirURL == null)
                throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

            fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
            fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

            String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
            String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
            ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                    String.format("*.%s", extension));

            fileChooser.getExtensionFilters().add(extFilter);
            File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (selected != null) {
                dataFilePath = selected.toPath();
                save();
            } //else return false; // if user presses escape after initially selecting 'yes'
        } else
            save();
    }


}
