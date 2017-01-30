package de.schmaun.ourrecipes.Model;

import org.parceler.Parcel;
import org.parceler.Transient;

@Parcel(Parcel.Serialization.BEAN)
public class RecipeImage {

    private long Id;

    private long recipeId;

    private String name;

    private String description;

    private String location;

    private int position;

    private int isCoverImage;

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

        return clone;
    }
}