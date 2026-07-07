package com.victusstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoDBRegionResponse {
    private List<RegionData> data;
    
    @Data
    public static class RegionData {
        private String name;
        @JsonProperty("isoCode")
        private String isoCode;
        @JsonProperty("wikiDataId")
        private String wikiDataId;
    }
}
