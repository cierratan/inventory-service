package com.sunright.inventory.entity.bombypj;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "BOMBYPJ")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Bombypj extends BaseEntity {

    @EmbeddedId
    private BombypjId id;

    @Version
    private Long version;

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

    @Column(name = "STATUS_1")
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
