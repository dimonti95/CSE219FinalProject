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

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    FILE_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    FILE_NOT_FOUND_TITLE,
    INVALID_INPUT_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    INVALID_INPUT,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,

    /* added */
    CHART_TITLE,
    TEXT_AREA_TITLE,
    DISPLAY_BUTTON_NAME,
    YES_STRING,
    NO_STRING

}
