package com.victusstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoDBCityResponse {
    private List<CityData> data;
    private Metadata metadata;
    
    @Data
    public static class CityData {
        private String name;
        @JsonProperty("regionCode")
        private String regionCode;
        @JsonProperty("wikiDataId")
        private String wikiDataId;
    }
    
    @Data
    public static class Metadata {
        @JsonProperty("currentOffset")
        private int currentOffset;
        @JsonProperty("totalCount")
        private int totalCount;
        @JsonProperty("page")
        private int page;
        @JsonProperty("limit")
        private int limit;
    }
}
