package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.base.InvBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "LOC")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Location extends InvBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String loc;
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
