package com.victusstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Region response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO {
    private Integer id;
    private String name;
    private String regionCode;
}
