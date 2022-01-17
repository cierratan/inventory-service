package com.sunright.inventory.entity.company;

import com.sunright.inventory.entity.BaseIdEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "COMPANY")
@Data
@NoArgsConstructor
public class Company {

    @EmbeddedId
    private BaseIdEntity id;

    private String companyName;
    private String plantName;
    private String nameShort;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String pCode;
    private String telNo;
    private String telNo2;
    private String faxNo;
    private String faxNo2;
    private String currencyCode;
    private String businessNature;
    private String countryCode;
    private String countryName;
    private String regionCode;
    private String regionName;
    private String stateCode;
    private String stateName;
    private String stockLoc;
    private String dateFormat;
    private String accessIn;
    private String accessBom;
    private String accessMrp;
    private String accessWc;
    private String accessCrp;
    private String accessMps;
    private String accessSfc;
    private String accessCa;
    private String accessSop;
    private String accessPur;
    private String accessNl;
    private String accessSl;
    private String accessPl;
    private String accessPr;
    private String accessCad;
    private String accessSt;
    private String accessLot;
    private String accessEx;
    private String accessFa;
    private String accessPer;
    private String accessUtil;
    private String traderCode;
    private String taxRegnNo;
    private String freightPercent;
    private String handlingPercent;
    private String invoiceTitle;
    private String passwordChange;
    private String businessRegnNo;
    private String accessCp;
    private String taxCat;
    private String productVersion;
    private String gafVersion;
    private String currencyDecimal;
    private String tzname;
    private String systemEmail;
}
