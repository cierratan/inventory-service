package com.sunright.inventory.entity.bombypj;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "BOMBYPJ")
@Data
@NoArgsConstructor
public class Bombypj {

    @EmbeddedId
    private BombypjId id;

    private Integer seqNo;

    @Column(name = "REQD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    @Column(name = "RESV_QTY", precision = 16, scale = 4)
    private BigDecimal resvQty;

    @Column(name = "SHORT_QTY", precision = 16, scale = 4)
    private BigDecimal shortQty;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "IN_TRANSIT_QTY", precision = 16, scale = 4)
    private BigDecimal inTransitQty;

    @Column(name = "DELV_QTY", precision = 16, scale = 4)
    private BigDecimal delvQty;

    @Column(name = "OUTSTANDING_QTY", precision = 16, scale = 4)
    private BigDecimal outstandingQty;

    @Column(name = "ISSUED_QTY", precision = 16, scale = 4)
    private BigDecimal issuedQty;

    private Date delvDate;
    private String uom;

    @Column(name = "STATUS")
    private String statuz;

    @Column(name = "PICKED_QTY", precision = 16, scale = 4)
    private BigDecimal pickedQty;

    private String mrpStatus;
    private String buylistStatus;
    private String resvStatus;
    private String srpType;

    @Column(name = "MRV_RESV", precision = 16, scale = 4)
    private BigDecimal mrvResv;

    @Column(name = "MRV_QTY", precision = 16, scale = 4)
    private BigDecimal mrvQty;

    @Column(name = "QTY_PER_ASSM", precision = 16, scale = 4)
    private BigDecimal qtyPerAssm;

    private String rpcNo;

    @Column(name = "RPC_RESV", precision = 16, scale = 4)
    private BigDecimal rpcResv;

    private String eqvStatus;
    private String loc;
}
