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
    RUN_BUTTON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    LINE_OF_ERROR,
    DUPLICATE_ERROR,

    /* warning messages */
    EXIT_WHILE_RUNNING_WARNING,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    LOAD_WORK_TITLE,
    SAVE_SCRNSHOT_TITLE,
    RUNNING_ALGORITHM_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    SCRNSHOT_FILE_EXT,
    SCRNSHOT_FILE_EXT_DESC,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    TEXT_AREA_LOADNAME,

    /* application-specific button names */
    CLUSTERING_BUTTON,
    CLASSIFICATION_BUTTON,
    DONE_BUTTON,
    EDIT_BUTTON,
    NEXT_INTERVAL_BUTTON,

    /* application-specific label */
    CLUSTERING_LABEL,
    CLASSIFICATION_LABEL,
    ALGORITHM_TYPE_LABEL,

    /* application-specific meta-data message */
    INSTANCES_MESSAGE,
    PATH_MESSAGE,
    LABEL_MESSAGE,

    /* application-specific classification run config */
    RUN_CONFIG_WINDOW_LABEL,
    CLASSIFICATION_RUN_CONFIG_LABEL,
    CLUSTERING_RUN_CONFIG_LABEL,
    MAX_ITERATIONS_LABEL,
    UPDATE_INTERVAL_LABEL,
    CONTINUOUS_RUN_LABEL,
    DISTINCT_LABELS_LABEL,
    SAVE_BUTTON,
    DEFAULT_ITERATIONS_VAL,
    DEFAULT_INTERVAL_VAL,

    /* application-specific series names */
    CLASSIFIER_SERIES,

    /* clustering algorithm names */
    CLUST_ALGORITHM_1_LOCATION,
    CLUST_ALGORITHM_2_LOCATION,

    /* classification algorithm names */
    CLASS_ALGORITHM_1_LOCATION,

    }
