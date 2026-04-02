package com.victusstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding a custom city
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCityRequest {
    private String name;
    private String regionCode;
}
