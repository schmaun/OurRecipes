package de.schmaun.ourrecipes.Model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Recipe {

    private long Id;

    private int categoryId;

    private String name;

    private String description;

    private String ingredients;

    private String preparation;

    private String notes;

    private int rating;

    private ArrayList<RecipeImage> images;

    private ArrayList<RecipeImage> imagesToDelete;

    private ArrayList<Label> labels;

    private boolean isFavourite;

    private Date createdAt;

    private Date lastEditAt;

    public Recipe() {
        this.images = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ArrayList<RecipeImage> getImages() {
        return images;
    }

    public void setImages(ArrayList<RecipeImage> images) {
        this.images = images;
    }

    public void addImage(RecipeImage image) {
        this.images.add(image);
    }

    public ArrayList<RecipeImage> getImagesToDelete() {
        return imagesToDelete;
    }

    public void setImagesToDelete(ArrayList<RecipeImage> images) {
        this.imagesToDelete = images;
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<Label> labels) {
        this.labels = labels;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public int getFavourite() {
        return isFavourite ? 1 : 0;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public void setFavourite(int favourite) {
        isFavourite = (favourite == 1);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastEditAt() {
        return lastEditAt;
    }

    public void setLastEditAt(Date lastEditAt) {
        this.lastEditAt = lastEditAt;
    }

}