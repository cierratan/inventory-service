package com.sunright.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfile {
    private String username;
    private String companyCode;
    private Integer plantNo;
}
