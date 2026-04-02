package com.victusstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for City response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {
    private Integer id;
    private String name;
    private String regionCode;
    private Boolean isOther;
}
