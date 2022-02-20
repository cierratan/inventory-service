package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunright.inventory.entity.enums.Closure;
import com.sunright.inventory.entity.enums.Obsolete;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class ItemDTO extends InvBaseDTO {
    private Boolean rohsStatus;

    private String itemNo;
    private String loc;
    private String source;
    private String partNo;
    private String mslCode;
    private String refUrl;
    private String description;
    private String manufacturer;
    private String uom;
    private String productGroup;
    private String issueNo;
    private String rev;

    private String categoryCode;
    private String categorySubCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String alternate;
    private Closure openClose;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date closeDate;
    private String obsoleteItem;
    private String qryObsItem;
    private Obsolete obsoleteCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date obsoleteDate;

    private String dimension;
    private String remarks;
    private BigDecimal stdMaterial;
    private BigDecimal balbfQty;
    private BigDecimal reorder;
    private BigDecimal leadtime;
    private String requestor;
    private String storageShelf;
    private BigDecimal qoh;
    private BigDecimal prodnResv;
    private BigDecimal orderQty;
    private BigDecimal batchNo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal eoh;
}
