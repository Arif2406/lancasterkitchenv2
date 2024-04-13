package com.example.demo1;
public class Recipe {
    private int id;
    private String name;
    private String description;
    private String pictureURL;

    public Recipe(int id, String name, String description, String pictureURL) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pictureURL = pictureURL;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPictureURL() {
        return pictureURL;
    }
}
