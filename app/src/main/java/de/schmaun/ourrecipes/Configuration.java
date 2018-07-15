package de.schmaun.ourrecipes;

public final class Configuration {
    public final static String PREFERENCES_NAME = "de.schmaun.ourrecipes.backup";

    public final static String PREF_KEY_LAST_BACKUP_DATE = "lastBackup";
    public final static String PREF_KEY_BACKUP_STATUS = "backupStatus";
    public final static int PREF_KEY_BACKUP_STATUS_SUCCESS = 1;
    public final static int PREF_KEY_BACKUP_STATUS_ERROR = 2;
    public final static int PREF_KEY_BACKUP_STATUS_RUNNING = 3;
    public final static String PREF_KEY_LAST_BACKUP_MESSAGE = "lastBackupMessage";


    public static final String IMAGE_PATH = "images";
    public static final String IMAGE_ENDING = ".jpg";
    public static final String IMAGE_MIME_TYPE = "image/jpeg";

    public static final boolean FEATURE_USE_GOOGLE_DRIVE_APPFOLDER = true;
    public static final boolean FEATURE_FAIL_BACKUP = false;

    public final static String FILE_AUTHORITY_IMAGES = "de.schmaun.fileprovider";
}
