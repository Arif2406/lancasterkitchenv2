
package com.example.demo1;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

/**
 * Class for representing a recipe with an ID, name, and description.
 */
public class Recipe {
    // Property to hold the unique ID of the recipe
    private final SimpleIntegerProperty recipeID;
    // Property to hold the name of the recipe
    private final SimpleStringProperty name;
    // Property to hold the description of the recipe
    private final SimpleStringProperty description;

    /**
     * Constructor to initialize a Recipe object with specified ID, name, and description.
     * @param recipeID The ID of the recipe.
     * @param name The name of the recipe.
     * @param description The description of the recipe.
     */
    public Recipe(int recipeID, String name, String description) {
        this.recipeID = new SimpleIntegerProperty(recipeID);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
    }

    /**
     * Sets the recipe ID.
     * @param recipeID The new ID to set.
     */
    public void setRecipeID(int recipeID) {
        this.recipeID.set(recipeID);
    }

    /**
     * Returns the name property of the recipe.
     * @return A SimpleStringProperty representing the name of the recipe.
     */
    public SimpleStringProperty nameProperty() {
        return name;
    }

    /**
     * Sets the name of the recipe.
     * @param name The new name to set.
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Returns the description property of this recipe.
     * @return A SimpleStringProperty representing the description of the recipe.
     */
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    /**
     * Sets the description of the recipe.
     * @param description The new description to set.
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * Gets the recipe ID.
     * @return The integer value of the recipe ID.
     */
    public int getRecipeID() {
        return recipeID.get();
    }

    /**
     * Returns the recipe ID property.
     * @return An IntegerProperty representing the recipe's ID.
     */
    public IntegerProperty recipeIDProperty() {
        return recipeID;
    }

    /**
     * Gets the name of the recipe.
     * @return A string representing the name of the recipe.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Gets the description of the recipe.
     * @return A string representing the description of the recipe.
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * Constructor for creating a Recipe object with existing properties.
     * @param recipeID      The recipeID property.
     * @param name          The name of the recipe.
     * @param description   The description property.
     */
    public Recipe(SimpleIntegerProperty recipeID, String name, SimpleStringProperty description) {
        this.recipeID = recipeID;
        this.name = new SimpleStringProperty(name);
        this.description = description;
    }}