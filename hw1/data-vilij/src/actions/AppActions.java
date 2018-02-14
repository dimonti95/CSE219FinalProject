package actions;

import javafx.stage.FileChooser;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

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
                        (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.FILE_NOT_FOUND_TITLE.name()),
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
        //FileWriter writer;
        ConfirmationDialog.getDialog().show
                (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                applicationTemplate.manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));
        if(ConfirmationDialog.getDialog().getSelectedOption() == null) { return false; }
        if(ConfirmationDialog.getDialog().getSelectedOption().name().equalsIgnoreCase
                (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.NO_STRING.name()))) { return true; }
            if(ConfirmationDialog.getDialog().getSelectedOption().name().equalsIgnoreCase
                    (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.YES_STRING.name()))) {
                FileChooser fc = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter
                        (applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name()));
                fc.getExtensionFilters().add(extFilter);
                File selectedFile = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selectedFile == null) { throw new IOException(); }
                return true;
            }
            else { return false; }
    }


}
