package de.schmaun.ourrecipes.Model;

import java.util.ArrayList;

public class Category {

    private int id;
    private String title;
    private int parentId;

    private ArrayList<Category> childCategories = new ArrayList<Category>();

    public Category() {
    }

    public Category(String title, int parentId) {
        this.title = title;
        this.parentId = parentId;
    }

    public Category(int id, String title, int parentId) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
    }

    public Category(int id, String title, int parentId, ArrayList<Category> childCategories) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
        this.childCategories = childCategories;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public ArrayList<Category> getChildCategories() {
        return childCategories;
    }

    public void setChildCategories(ArrayList<Category> childCategories) {
        this.childCategories = childCategories;
    }

    public String toString() {
        return title;
    }

    public boolean hasParent() {
        return parentId > 0;
    }

}
