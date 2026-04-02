package com.victus.egyptregions.model;

/**
 * Model class representing a region in Egypt
 * Contains basic information about a region with unique code
 */
public class Region {
    private int id;
    private String name;
    private String regionCode;

    // Default constructor
    public Region() {}

    // Parameterized constructor
    public Region(int id, String name, String regionCode) {
        this.id = id;
        this.name = name;
        this.regionCode = regionCode;
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

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionCode='" + regionCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return id == region.id &&
                regionCode.equals(region.regionCode);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + regionCode.hashCode();
        return result;
    }
}
