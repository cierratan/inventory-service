package com.sunright.inventory.dto.bombypj;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class BombypjDTO extends BaseDTO {

    private String projectNo;
    private String orderNo;
    private String assemblyNo;
    private String component;
    private String alternate;

    private Long version;

    private Integer seqNo;
    private BigDecimal recdQty;
    private BigDecimal resvQty;
    private BigDecimal shortQty;
    private BigDecimal orderQty;
    private BigDecimal inTransitQty;
    private BigDecimal delvQty;
    private BigDecimal outstandingQty;
    private BigDecimal issuedQty;
    private Date delvDate;
    private String uom;
    private String statuz;
    private BigDecimal pickedQty;
    private String mrpStatus;
    private String buylistStatus;
    private String resvStatus;
    private String srpType;
    private BigDecimal mrvResv;
    private BigDecimal mrvQty;
    private BigDecimal qtyPerAssm;
    private String rpcNo;
    private BigDecimal rpcResv;
    private String eqvStatus;
    private String loc;
}
