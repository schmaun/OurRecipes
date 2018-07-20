package de.schmaun.ourrecipes.backup;

import com.google.android.gms.drive.DriveId;

import java.util.Date;

public class Backup {
    private String title;

    private Date createdAt;
    private DriveId folderId;

    public String getTitle() {
        return title;
    }

    public Backup setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Backup setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public DriveId getFolderId() {
        return this.folderId;
    }

    public Backup setFolderId(DriveId folderId) {
        this.folderId = folderId;

        return this;
    }
}
