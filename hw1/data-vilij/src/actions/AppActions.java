package actions;

import javafx.stage.FileChooser;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        try {
            if(promptToSave())  { clearOldData(); }
        } catch (IOException e) {
                ErrorDialog.getDialog().show
                        (applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.FILE_NOT_FOUND.name()));
            }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        applicationTemplate.getUIComponent().getPrimaryWindow().close();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /** added methods */
    public void clearOldData(){
        ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
        if(((AppUI) applicationTemplate.getUIComponent()).getHasNewText()) {
            ((AppUI) applicationTemplate.getUIComponent()).setHasNewText(false);
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
        String relativePath = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH2.name())
                + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());

        dataFilePath = Paths.get(relativePath);

        applicationTemplate.getDialog(ConfirmationDialog.DialogType.CONFIRMATION).show
                (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if(ConfirmationDialog.getDialog().getSelectedOption() == null) { return false; }

        if(ConfirmationDialog.getDialog().getSelectedOption() == ConfirmationDialog.Option.NO) { return true; }
            if(ConfirmationDialog.getDialog().getSelectedOption() == ConfirmationDialog.Option.YES) {
                FileChooser fc = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter
                        (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));

                fc.setTitle(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name()));
                fc.getExtensionFilters().add(extFilter);

                if(!dataFilePath.toFile().isDirectory()) { applicationTemplate.getDialog(ErrorDialog.DialogType.ERROR).show
                        (applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                                applicationTemplate.manager.getPropertyValue
                                        (AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name())); }

                fc.setInitialDirectory(dataFilePath.toFile());
                File savedFile = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

                if (savedFile == null) { throw new IOException(); }

                FileWriter writer = new FileWriter(savedFile);
                writer.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
                writer.close();
                return true;
            }
            else { return false; }
    }


}
