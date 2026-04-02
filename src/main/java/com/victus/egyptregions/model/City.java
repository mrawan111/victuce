package com.victus.egyptregions.model;

/**
 * Model class representing a city in Egypt
 * Contains city information with region association and Other option support
 */
public class City {
    private int id;
    private String name;
    private String regionCode;
    private boolean isOther;

    // Default constructor
    public City() {}

    // Parameterized constructor
    public City(int id, String name, String regionCode, boolean isOther) {
        this.id = id;
        this.name = name;
        this.regionCode = regionCode;
        this.isOther = isOther;
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

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public boolean isOther() {
        return isOther;
    }

    public void setOther(boolean other) {
        isOther = other;
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionCode='" + regionCode + '\'' +
                ", isOther=" + isOther +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id &&
                isOther == city.isOther &&
                name.equals(city.name) &&
                regionCode.equals(city.regionCode);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + regionCode.hashCode();
        result = 31 * result + (isOther ? 1 : 0);
        return result;
    }
}
