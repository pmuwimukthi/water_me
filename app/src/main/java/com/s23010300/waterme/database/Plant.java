package com.s23010300.waterme.database;

public class Plant {
    private int id;
    private String name;
    private String imageName;
    private String description;
    private int wateringFrequency; // times per week
    private String lastWatered;
    private boolean missedLastWatering;

    public Plant() {}

    public Plant(String name, String imageName, String description, int wateringFrequency) {
        this.name = name;
        this.imageName = imageName;
        this.description = description;
        this.wateringFrequency = wateringFrequency;
        this.missedLastWatering = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWateringFrequency() {
        return wateringFrequency;
    }

    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }

    public String getLastWatered() {
        return lastWatered;
    }

    public void setLastWatered(String lastWatered) {
        this.lastWatered = lastWatered;
    }

    public boolean isMissedLastWatering() {
        return missedLastWatering;
    }

    public void setMissedLastWatering(boolean missedLastWatering) {
        this.missedLastWatering = missedLastWatering;
    }
}