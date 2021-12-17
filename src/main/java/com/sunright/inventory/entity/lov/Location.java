package com.sunright.inventory.entity.lov;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity(name = "LOC")
@Data
@NoArgsConstructor
public class Location {
    @EmbeddedId
    private LocationId locationId;

    @Version
    private Long version;

    private String description;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String pcode;
    private String telNo;
    private String faxNo;
    private String countryCode;
    private String regionCode;
    private String stateCode;
    private String cityCode;
    private String personInCharge;
    private String remarks;
}
