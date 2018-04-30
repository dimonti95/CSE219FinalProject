package actions;

import algorithms.DataSet;
import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static settings.AppPropertyTypes.LOAD_WORK_TITLE;
import static settings.AppPropertyTypes.SAVE_SCRNSHOT_TITLE;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.SEPARATOR;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    private Path dataFilePath;

    /** Name of the file most recently loaded **/
    private String loadedFileName;

    /** Path to the screenshot file currently active. */
    private Path scrnshotFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
    private SimpleBooleanProperty isUnsaved;

    /** The boolean property marking whether or not data has been loaded froma file. */
    private SimpleBooleanProperty wasLoaded;

    private ArrayList<String>   firstTenLines;

    /** setters */
    public void   setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }
    public void   setWasLoadedProperty(boolean property) { wasLoaded.set(property); }
    public void   setLoadedFileName(String fileName)     { this.loadedFileName = fileName; }

    /** getters */
    public String getFileName()                          { return loadedFileName; }

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
        this.wasLoaded = new SimpleBooleanProperty(false);
    }

    @Override
    public void handleNewRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                ((AppUI) applicationTemplate.getUIComponent()).showNewDataUI();
                isUnsaved.set(false);
                dataFilePath  = null;
                wasLoaded.set(false);
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
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
        PropertyManager manager = applicationTemplate.manager;
        AppUI   appUI           = ((AppUI) applicationTemplate.getUIComponent());
        AppData dataComponent   = (AppData) applicationTemplate.getDataComponent();

        FileChooser fileChooser = new FileChooser();
        String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL         dataDirURL  = getClass().getResource(dataDirPath);

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(manager.getPropertyValue(LOAD_WORK_TITLE.name()));

        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                String.format("*.%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        wasLoaded.set(false);

        if(selected != null) {

            StringBuilder data = new StringBuilder();
            firstTenLines = new ArrayList<>(10);
            int i = 0;

            try {
                Scanner scanner = new Scanner(selected);

                while (scanner.hasNextLine()) {
                    String lineOfData = scanner.nextLine();
                    data.append(lineOfData).append(System.getProperty("line.separator"));
                    if (i < 10) {
                        firstTenLines.add(lineOfData);
                        i++;
                    }
                }

                loadedFileName = formatPathString(selected.getPath());

                applicationTemplate.getUIComponent().clear();
                ((AppData) applicationTemplate.getDataComponent()).loadData(data.toString());
                boolean duplicateFound = appUI.duplicateFound;

                if(!duplicateFound && dataComponent.getDataIsValid()) {
                    ((AppUI) applicationTemplate.getUIComponent()).setLoadedDataUI();
                    outputDataToTxtArea();
                    ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
                    wasLoaded.set(true);
                    isUnsaved.set(false);
                } else if(duplicateFound || !dataComponent.getDataIsValid()) { appUI.showNewDataUI(); }

            } catch (FileNotFoundException e) {
                loadErrHandlingHelper();
            }
        }
    }

    private String formatPathString(String path){
        StringBuilder sb = new StringBuilder();
        String[] parts = { path.substring(0, path.length()/3), path.substring(path.length()/3,
                        (path.length()/3)*2), path.substring((path.length()/3)*2) };
        sb.append(parts[0]).append("\n").append(parts[1]).append("\n").append(parts[2]);
        return sb.toString(); }

    @Override
    public void handleExitRequest() {
        if(promptToTerminateAlgorithm()) {
            try {
                if (!isUnsaved.get() || promptToSave())
                    System.exit(0);
            } catch (IOException e) {
                errorHandlingHelper();
            }
        }
    }

    @Override
    public void handlePrintRequest() { }

    public void handleScreenshotRequest() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        LineChart<Number, Number> chart = ((AppUI) applicationTemplate.getUIComponent()).getChart();

        WritableImage writableImage = new WritableImage((int)chart.getWidth(), (int)chart.getHeight());
        chart.snapshot(null, writableImage);

        if(scrnshotFilePath == null){
            FileChooser fileChooser = new FileChooser();
            String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
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
                throw new IOException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
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
                String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
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
        wasLoaded.set(false);
    }

    /** Application Confirmation Dialogs/Error Dialogs */
    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        String          newLine  = System.getProperty("line.separator") + System.getProperty("line.separator");
        String          lineMsg  = manager.getPropertyValue(AppPropertyTypes.LINE_OF_ERROR.name());
        AtomicInteger   errLine  = ((AppData) applicationTemplate.getDataComponent()).getTSDProcessor().lineOfError;
        dialog.show(errTitle, errMsg + errInput + newLine + lineMsg + errLine.get());
    }

    private void loadErrHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }

    public void saveErrHandlingHelper(){
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }

    public void duplicateHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        String          newLine  = System.getProperty("line.separator") + System.getProperty("line.separator");
        String          dupeMsg  = manager.getPropertyValue(AppPropertyTypes.DUPLICATE_ERROR.name());
        String          dupe     = ((AppUI) applicationTemplate.getUIComponent()).duplicate;
        dialog.show(errTitle, errMsg + errInput + newLine + dupeMsg + dupe);
    }

    private void outputDataToTxtArea(){
        TextArea textArea = ((AppUI) applicationTemplate.getUIComponent()).getTextArea();
        for (String firstTenLine : firstTenLines) {
            if (firstTenLine == null) { break; }
            else { textArea.appendText(firstTenLine + System.getProperty("line.separator")); }
        }
    }

    private void saveRequestHandler() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        if (dataFilePath == null) {
            FileChooser fileChooser = new FileChooser();
            String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
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
            }
        } else
            save();
    }

    private boolean promptToTerminateAlgorithm() {
        PropertyManager manager = applicationTemplate.manager;
        AppUI uiComponent = ((AppUI) applicationTemplate.getUIComponent());
        ConfirmationDialog dialog = ConfirmationDialog.getDialog();

        /* checking if an algorithm is running */
        if (uiComponent.getAlgorithmThread() == null || !uiComponent.getAlgorithmThread().isAlive()) {
            return true;
        }

        dialog.show(manager.getPropertyValue(AppPropertyTypes.RUNNING_ALGORITHM_TITLE.name()),
                manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name()));

        if (dialog.getSelectedOption() == null) return false;

        /* exit without finishing the current algorithm run */
        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) return true;

        /* finish running algorithm */
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.NO) &&
                !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    /** Loads Tab Separated Data into the DataSet */
    public DataSet getLoadedDataSet(){
        DataSet dataSet        = new DataSet();
        AppData dataComponent  = (AppData) applicationTemplate.getDataComponent();
        Map<String, String>  dataLabels = dataComponent.getTSDProcessor().getDataLabels();
        Map<String, Point2D> dataPoints = dataComponent.getTSDProcessor().getDataPoints();
        dataLabels.forEach((x,y) -> dataSet.getLabels().put(x, y));
        dataPoints.forEach((x,y) -> dataSet.getLocations().put(x, y));
        return dataSet;
    }


}
