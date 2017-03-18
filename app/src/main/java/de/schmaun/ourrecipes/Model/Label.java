package de.schmaun.ourrecipes.Model;

import java.io.Serializable;

public class Label implements Serializable {

    private long id;
    private String name;
    private int countRecipes;
    private String imageLocation;
    private boolean fullSpan;

    private int importance;

    public Label(){}

    public Label(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() { return name; }

    public int getCountRecipes() {
        return countRecipes;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public void setCountRecipes(int countRecipes) {
        this.countRecipes = countRecipes;
    }

    public void setFullSpan(boolean fullSpan) {
        this.fullSpan = fullSpan;
    }

    public boolean isFullSpan() {
        return fullSpan;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }
}
