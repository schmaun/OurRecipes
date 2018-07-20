package de.schmaun.ourrecipes.backup;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.schmaun.ourrecipes.Configuration;
import de.schmaun.ourrecipes.Database.DbHelper;
import de.schmaun.ourrecipes.Database.RecipeImageRepository;
import de.schmaun.ourrecipes.Database.RecipeRepository;
import de.schmaun.ourrecipes.Exception.NotSignedInException;
import de.schmaun.ourrecipes.Model.Recipe;
import de.schmaun.ourrecipes.Model.RecipeImage;
import de.schmaun.ourrecipes.Utils.Stream;

import static de.schmaun.ourrecipes.Utils.File.getMimeTypeFromUrl;

public class GoogleDriveBackup {
    private static final String TAG = "GoogleDriveBackup";
    private static final String BACKUP_ROOT_FOLDER_NAME = "OurRecipesBackup";
    private Context context;
    private DriveResourceClient driveResourceClient;

    interface OnResultListener {
        void onSuccess();

        void onError(Exception e);
    }

    interface LoadBackupsOnResultListener {
        void onSuccess(ArrayList<Backup> backups);

        void onError(Exception e);
    }

    GoogleDriveBackup(Context context) {
        this.context = context;
    }

    public void backup(OnResultListener onResultListener) {
        try {
            initDriveClient();

            if (Configuration.FEATURE_USE_GOOGLE_DRIVE_APPFOLDER) {
                Task<DriveFolder> appFolder = getDriveResourceClient().getAppFolder();
                doBackup(onResultListener, appFolder);
            } else {
                int numCores = Runtime.getRuntime().availableProcessors();
                ThreadPoolExecutor executor = new ThreadPoolExecutor(numCores * 2, numCores * 2,
                        60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

                loadApplicationRoot()
                        .addOnSuccessListener(executor, rootMetadataBuffer -> {
                            DriveFolder appFolder = null;
                            for (Metadata row : rootMetadataBuffer) {
                                if (row.isFolder()) {
                                    appFolder = row.getDriveId().asDriveFolder();
                                    break;
                                }
                            }

                            if (appFolder != null) {
                                doBackup(onResultListener, appFolder);
                            } else {
                                Task<DriveFolder> applicationRoot = createApplicationRoot();
                                doBackup(onResultListener, applicationRoot);
                            }
                        });
            }
        } catch (NotSignedInException e) {
            onResultListener.onError(e);
        }
    }

    public void restoreDatabase(DriveId folderId, OnResultListener onResultListener) {
        try {
            initDriveClient();

            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, DbHelper.DATABASE_NAME))
                    .build();

            getDriveResourceClient().queryChildren(folderId.asDriveFolder(), query)
                    .continueWithTask(task -> {
                        DriveFile file = null;
                        if (task.getResult().getCount() == 1) {
                            file = task.getResult().get(0).getDriveId().asDriveFile();
                        }
                        if (file == null) {
                            throw new Exception("No database found. Whoa?");
                        }

                        return getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);
                    }).addOnSuccessListener(driveContents -> {
                        try {
                            File database = context.getDatabasePath(DbHelper.DATABASE_NAME);
                            OutputStream output = new FileOutputStream(database);
                            Stream.copy(driveContents.getInputStream(), output);

                            onResultListener.onSuccess();
                        } catch (java.io.IOException e) {
                            Log.e(TAG, "restoreDatabase failed: IOException", e);
                            onResultListener.onError(e);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "restoreDatabase failed", e);
                        onResultListener.onError(e);
                    });
        } catch (NotSignedInException e) {
            onResultListener.onError(e);
        }
    }

    private void doBackup(OnResultListener onResultListener, Task<DriveFolder> parentFolder) {
        try {
            Task<DriveFolder> backupFolderTask = createBackupFolder(parentFolder);
            Task<DriveFolder> imageFolderTask = createImageFolder(backupFolderTask);

            Tasks.await(Tasks.whenAll(
                    uploadDatabase(backupFolderTask),
                    uploadRecipes(backupFolderTask)
            ));
            Tasks.await(imageFolderTask);
            uploadImages(imageFolderTask);

            onResultListener.onSuccess();
        } catch (ExecutionException | InterruptedException e) {
            onResultListener.onError(e);
        }
    }

    private void doBackup(OnResultListener onResultListener, DriveFolder parentFolder) {
        try {
            Task<DriveFolder> backupFolderTask = createBackupFolder(parentFolder);
            Task<DriveFolder> imageFolderTask = createImageFolder(backupFolderTask);

            Tasks.await(Tasks.whenAll(
                    uploadDatabase(backupFolderTask),
                    uploadRecipes(backupFolderTask)
            ));
            Tasks.await(imageFolderTask);
            uploadImages(imageFolderTask);

            onResultListener.onSuccess();
        } catch (ExecutionException | InterruptedException e) {
            onResultListener.onError(e);
        }
    }

    public void loadBackups(int max, final LoadBackupsOnResultListener onResultListener) {
        try {
            initDriveClient();
        } catch (NotSignedInException e) {
            onResultListener.onError(e);
        }

        final Task<DriveFolder> folder;
        if (Configuration.FEATURE_USE_GOOGLE_DRIVE_APPFOLDER) {
            folder = getDriveResourceClient().getAppFolder();

            folder.continueWithTask(task -> {
                return getDriveResourceClient().listChildren(task.getResult());
            })
                    .addOnSuccessListener(metadataBuffer -> {
                        onResultListener.onSuccess(getBackupsFromBuffer(max, metadataBuffer));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "loading backups (loadBackups)", e);
                        onResultListener.onError(e);
                    });
        } else {
            loadApplicationRoot()
                    .addOnSuccessListener(rootMetadataBuffer -> {
                        DriveFolder driveFolder = null;
                        for (Metadata row : rootMetadataBuffer) {
                            if (row.isFolder()) {
                                driveFolder = row.getDriveId().asDriveFolder();
                                break;
                            }
                        }

                        if (driveFolder != null) {
                            getDriveResourceClient().listChildren(driveFolder)
                                    .addOnSuccessListener(metadataBuffer -> {
                                        onResultListener.onSuccess(getBackupsFromBuffer(max, metadataBuffer));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "loading backups (loadBackups)", e);
                                        onResultListener.onError(e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "loading application root (loadBackups)", e);
                        onResultListener.onError(e);
                    });
        }
    }

    @NonNull
    private ArrayList<Backup> getBackupsFromBuffer(int max, MetadataBuffer metadataBuffer) {
        ArrayList<Backup> backups = new ArrayList<>();
        int numOfBackups = max < metadataBuffer.getCount() ? max : metadataBuffer.getCount();
        for (int i = 0; i < numOfBackups; i++) {
            Metadata row = metadataBuffer.get(i);
            backups.add(new Backup()
                    .setCreatedAt(row.getCreatedDate())
                    .setFolderId(row.getDriveId())
                    .setTitle(row.getTitle()));
        }
        metadataBuffer.release();

        return backups;
    }

    private void initDriveClient() throws NotSignedInException {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this.context);
        if (signInAccount == null) {
            throw new NotSignedInException();
        }

        driveResourceClient = Drive.getDriveResourceClient(this.context.getApplicationContext(), signInAccount);
    }

    @NonNull
    private Task<DriveFile> uploadDatabase(Task<DriveFolder> backupFolderTask) {
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

        return Tasks.whenAll(backupFolderTask, createContentsTask).continueWithTask(
                task -> {
                    File database = context.getDatabasePath(DbHelper.DATABASE_NAME);
                    DriveFolder folder = backupFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();

                    Stream.copy(new FileInputStream(database), contents.getOutputStream());

                    MetadataChangeSet.Builder changeSetBuilder = new MetadataChangeSet.Builder()
                            .setTitle(DbHelper.DATABASE_NAME)
                            .setMimeType("application/x-sqlite3");

                    return getDriveResourceClient().createFile(folder, changeSetBuilder.build(), contents);
                })
                .addOnSuccessListener(driveFolder -> Log.d(TAG, "uploadDatabase finished"))
                .addOnFailureListener(e -> Log.e(TAG, "uploadDatabase failed", e));
    }

    @NonNull
    private Task<DriveFile> uploadRecipes(Task<DriveFolder> backupFolderTask) {
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        final List<Recipe> recipes = RecipeRepository.getInstance(new DbHelper(this.context)).getForBackup();

        return Tasks.whenAll(backupFolderTask, createContentsTask).continueWithTask(
                task -> {
                    DriveFolder folder = backupFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    try (Writer writer = new OutputStreamWriter(contents.getOutputStream())) {
                        writer.write(gson.toJson(recipes));
                    }

                    MetadataChangeSet.Builder changeSetBuilder = new MetadataChangeSet.Builder()
                            .setTitle("recipes.json")
                            .setMimeType("application/json");

                    return getDriveResourceClient().createFile(folder, changeSetBuilder.build(), contents);
                })
                .addOnSuccessListener(driveFolder -> Log.d(TAG, "uploadRecipes finished"))
                .addOnFailureListener(e -> Log.e(TAG, "uploadRecipes failed", e));
    }

    private void uploadImages(Task<DriveFolder> imageFolderTask) throws ExecutionException, InterruptedException {
        Collection<Task<?>> tasks = new ArrayList<>();
        ArrayList<RecipeImage> images = RecipeImageRepository.getInstance(new DbHelper(this.context)).getForBackup();

        Log.d(TAG, "uploadImages: starting upload of " + images.size() + " images");

        for (RecipeImage image : images) {
            final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();

            tasks.add(createContentsTask.continueWithTask(new UploadImage(image, imageFolderTask.getResult()))
                    .addOnSuccessListener(result -> {
                        Log.d(TAG, String.format("Image uploaded: %s -> %s", Long.toString(image.getId()), result.getDriveId().encodeToString()));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Image upload failed: " + Long.toString(image.getId()), e))
            );
        }

        Tasks.await(Tasks.whenAll(tasks));

        Log.d(TAG, "uploadImages: finished upload");
    }

    @NonNull
    private Task<DriveFolder> createImageFolder(Task<DriveFolder> backupFolderTask) {
        return backupFolderTask.continueWithTask(
                task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("images")
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .build();

                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(driveFolder -> {
                    Log.d(TAG, "Folder created (createImageFolder): " + driveFolder.getDriveId().encodeToString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Unable to create folder (createImageFolder)", e));
    }

    @NonNull
    private Task<DriveFolder> createBackupFolder(Task<DriveFolder> appFolderTask) {
        return appFolderTask.continueWithTask(
                task -> createBackupFolder(task.getResult()));
    }

    @NonNull
    private Task<DriveFolder> createBackupFolder(DriveFolder parentFolder) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

        Date date = new Date();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("Backup " + (simpleDateFormat.format(date)))
                .setMimeType(DriveFolder.MIME_TYPE)
                .setCustomProperty(new CustomPropertyKey("backupDate", CustomPropertyKey.PUBLIC), Long.toString(date.getTime()))
                .build();

        return getDriveResourceClient().createFolder(parentFolder, changeSet)
                .addOnSuccessListener(driveFolder -> {
                    Log.d(TAG, "Folder created (createBackupFolder): " + driveFolder.getDriveId().encodeToString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Unable to create folder (createBackupFolder)", e));
    }

    private Task<MetadataBuffer> loadApplicationRoot() {
        return getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    Query query = new Query.Builder()
                            .addFilter(Filters.eq(SearchableField.TITLE, BACKUP_ROOT_FOLDER_NAME))
                            .build();

                    return getDriveResourceClient().queryChildren(task.getResult(), query);
                });
    }

    @NonNull
    private Task<DriveFolder> createApplicationRoot() {
        return getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(BACKUP_ROOT_FOLDER_NAME)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .build();
                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(driveFolder -> {
                    Log.d(TAG, "Folder created (createApplicationRoot): " + driveFolder.getDriveId().encodeToString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Unable to create folder (createApplicationRoot)", e));
    }

    private DriveResourceClient getDriveResourceClient() {
        return driveResourceClient;
    }

    public class UploadImage implements Continuation<DriveContents, Task<DriveFile>> {
        final private RecipeImage image;
        final private DriveFolder folder;

        UploadImage(RecipeImage image, DriveFolder folder) {
            this.image = image;
            this.folder = folder;
        }

        @Override
        public Task<DriveFile> then(@NonNull Task<DriveContents> createContentsTask) throws Exception {
            DriveContents contents = createContentsTask.getResult();
            String imageLocation = image.getLocation(context);
            InputStream in = context.getContentResolver().openInputStream(Uri.parse(imageLocation));
            Stream.copy(in, contents.getOutputStream());

            if (Configuration.FEATURE_FAIL_BACKUP && image.getId() == 1) {
                throw new Exception("FEATURE_FAIL_BACKUP is enabled!");
            }

            String mimeType = getMimeTypeFromUrl(imageLocation);
            MetadataChangeSet.Builder changeSetBuilder = new MetadataChangeSet.Builder()
                    .setTitle(image.getFileName())
                    .setCustomProperty(new CustomPropertyKey("id", CustomPropertyKey.PUBLIC), Long.toString(image.getId()))
                    .setCustomProperty(new CustomPropertyKey("recipeId", CustomPropertyKey.PUBLIC), Long.toString(image.getRecipeId()));

            if (mimeType != null) {
                changeSetBuilder.setMimeType(mimeType);
            }

            return getDriveResourceClient().createFile(folder, changeSetBuilder.build(), contents);
        }
    }
}
