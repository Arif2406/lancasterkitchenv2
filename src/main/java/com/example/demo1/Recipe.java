package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

public class Recipe {
    private final SimpleIntegerProperty recipeID;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;

    public Recipe(int recipeID, String name, String description) {
        this.recipeID = new SimpleIntegerProperty(recipeID);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
    }

    public void setRecipeID(int recipeID) {
        this.recipeID.set(recipeID);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public int getRecipeID() {
        return recipeID.get();
    }

    public IntegerProperty recipeIDProperty() {
        return recipeID;
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public Recipe(SimpleIntegerProperty recipeID, String name, SimpleStringProperty description) {
        this.recipeID = recipeID;
        this.name = new SimpleStringProperty(name);
        this.description = description;
    }
}