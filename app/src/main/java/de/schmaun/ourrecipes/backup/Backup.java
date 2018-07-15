package de.schmaun.ourrecipes.backup;

import java.util.Date;

public class Backup {
    private String title;

    private Date createdAt;

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
}
