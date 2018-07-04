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
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    private Context context;
    private DriveResourceClient driveResourceClient;

    interface OnResultListener {
        public void onSuccess();

        public void onError(Exception e);
    }

    GoogleDriveBackup(Context context) {
        this.context = context;
    }

    public void doBackup(OnResultListener onResultListener) {
        final Task<DriveFolder> appFolderTask;

        try {
            initDriveClient();
            if (Configuration.FEATURE_USE_GDRIVE_APPFOLDER) {
                appFolderTask = getDriveResourceClient().getAppFolder();
            } else {
                appFolderTask = createApplicationRoot();
            }

            Task<DriveFolder> backupFolderTask = createBackupFolder(appFolderTask);
            Task<DriveFolder> imageFolderTask = createImageFolder(backupFolderTask);

            Tasks.await(Tasks.whenAll(
                    uploadDatabase(backupFolderTask), uploadRecipes(backupFolderTask)
            ));
            Tasks.await(imageFolderTask);
            uploadImages(imageFolderTask);

            onResultListener.onSuccess();
        } catch (NotSignedInException | ExecutionException | InterruptedException e) {
            onResultListener.onError(e);
        }
    }

    private void initDriveClient() throws NotSignedInException {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this.context);
        if (signInAccount == null) {
            throw new NotSignedInException();
        }

        driveResourceClient = Drive.getDriveResourceClient(this.context.getApplicationContext(), signInAccount);
    }


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
                task -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

                    DriveFolder parentFolder = task.getResult();
                    Date date = new Date();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("Backup " + (simpleDateFormat.format(date)))
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setCustomProperty(new CustomPropertyKey("backupDate", CustomPropertyKey.PUBLIC), Long.toString(date.getTime()))
                            .build();

                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(driveFolder -> {
                    Log.d(TAG, "Folder created (createBackupFolder): " + driveFolder.getDriveId().encodeToString());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Unable to create folder (createBackupFolder)", e));
    }

    @NonNull
    private Task<DriveFolder> createApplicationRoot() {
        return getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("OurRecipesBackup")
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
            InputStream in = context.getContentResolver().openInputStream(Uri.parse(image.getLocation()));
            Stream.copy(in, contents.getOutputStream());

            String mimeType = getMimeTypeFromUrl(image.getLocation());
            MetadataChangeSet.Builder changeSetBuilder = new MetadataChangeSet.Builder()
                    .setTitle(Long.toString(image.getId()))
                    .setCustomProperty(new CustomPropertyKey("id", CustomPropertyKey.PUBLIC), Long.toString(image.getId()))
                    .setCustomProperty(new CustomPropertyKey("recipeId", CustomPropertyKey.PUBLIC), Long.toString(image.getRecipeId()));

            if (mimeType != null) {
                changeSetBuilder.setMimeType(mimeType);
            }

            return getDriveResourceClient().createFile(folder, changeSetBuilder.build(), contents);
        }
    }
}
