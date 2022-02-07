package com.sunright.inventory.dto.sale;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class SaleDetailDTO {

    private String companyCode;
    private Integer plantNo;
    private String orderNo;
    private int recSeq;

    private String projectNoSub;
    private String productType;
    private String productSubType;
    private String productDesc;
}
