package de.schmaun.ourrecipes.Model;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.net.URL;

@Parcel(Parcel.Serialization.BEAN)
public class RecipeImage {

    private long Id;

    private long recipeId;

    private String name;

    private String description;

    private String location;

    private int position;

    private int isCoverImage;

    private int parentType;

    public static final int PARENT_TYPE_INGREDIENTS = 1;

    public static final int PARENT_TYPE_PREPARATION = 2;

    public RecipeImage() {
    }

    public RecipeImage(String location) {
        this.location = location;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public int getCoverImage() {
        return isCoverImage;
    }

    public void setCoverImage(int coverImage) {
        isCoverImage = coverImage;
    }

    @Transient
    public void setCoverImage(boolean coverImage) {
        isCoverImage = coverImage ? 1 : 0;
    }

    @Transient
    public boolean isCoverImage()
    {
        return (isCoverImage == 1);
    }

    public int getParentType() {
        return parentType;
    }

    public void setParentType(int parentType) {
        this.parentType = parentType;
    }

    public RecipeImage clone()
    {
        RecipeImage clone = new RecipeImage();
        clone.setId(this.getId());
        clone.setDescription(this.getDescription());
        clone.setLocation(this.getLocation());
        clone.setName(this.getName());
        clone.setPosition(this.getPosition());
        clone.setRecipeId(this.getRecipeId());
        clone.setCoverImage(this.getCoverImage());
        clone.setParentType(this.getParentType());

        return clone;
    }
}