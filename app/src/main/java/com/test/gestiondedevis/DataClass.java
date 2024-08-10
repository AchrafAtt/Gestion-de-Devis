package com.test.gestiondedevis;

import java.util.Date;

public class DataClass {
    private String name;
    private String description;
    private Double price;
    private String image;
    private String condition;
    private String category;
    private Date date;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public DataClass(String name, String description, Double price, String image, String condition, String category, Date date) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.condition = condition;
        this.category = category;
        this.date = date;
    }

    public DataClass() {
    }
    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getCondition() {
        return condition;
    }

    public String getCategory() {
        return category;
    }

    public Date getDate() {
        return date;
    }
}

