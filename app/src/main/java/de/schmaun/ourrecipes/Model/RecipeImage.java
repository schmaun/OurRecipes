package de.schmaun.ourrecipes.Model;

public class RecipeImage {

    private long Id;

    private long recipeId;

    private String name;

    private String description;

    private String location;

    private int position;

    public RecipeImage() {
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

    public RecipeImage clone()
    {
        RecipeImage clone = new RecipeImage();
        clone.setId(this.getId());
        clone.setDescription(this.getDescription());
        clone.setLocation(this.getLocation());
        clone.setName(this.getName());
        clone.setPosition(this.getPosition());
        clone.setRecipeId(this.getId());

        return clone;
    }
}