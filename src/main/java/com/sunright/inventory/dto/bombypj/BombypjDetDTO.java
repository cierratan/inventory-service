package com.sunright.inventory.dto.bombypj;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class BombypjDetDTO {

    private String projectNo;
    private String orderNo;
    private String assemblyNo;
    private String component;
    private String alternate;

    private String poNo;
    private Integer seqNo;
    private BigDecimal resvQty;
    private BigDecimal accumRecdQty;
    private BigDecimal recdQty;
    private String status;
    private String grnNo;
}
