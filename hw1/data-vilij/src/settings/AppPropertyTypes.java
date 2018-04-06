package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,
    CSS_RESOURCE_FILENAME,

    /* user interface icon file names */
    SCREENSHOT_ICON,
    CONFIGURATION_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    LINE_OF_ERROR,
    DUPLICATE_ERROR,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    LOAD_WORK_TITLE,
    SAVE_SCRNSHOT_TITLE,
    TOTAL_DATA_LOADED_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    TOTAL_DATA_LOADED_MSG,
    TOTAL_DATA_MSG,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    SCRNSHOT_FILE_EXT,
    SCRNSHOT_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    DISPLAY_BUTTON_TEXT,
    RADIO_BUTTON_TEXT,
    AVERAGE_SERIES_NAME,
    LINES

}
