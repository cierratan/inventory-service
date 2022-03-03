package com.sunright.inventory.dto.lov;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class LocationDTO extends InvBaseDTO {

    @NotBlank(message = "Location must not be empty")
    private String loc;

    private String description;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String pcode;
    private String telNo;
    private String faxNo;

    @NotBlank(message = "Country Code must not be empty")
    private String countryCode;
    private String regionCode;
    private String stateCode;
    private String cityCode;
    private String personInCharge;
    private String remarks;

    private String countryName;
}
