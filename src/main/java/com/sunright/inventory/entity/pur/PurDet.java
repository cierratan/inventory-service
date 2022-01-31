package com.sunright.inventory.entity.pur;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "PURDET")
@Data
@NoArgsConstructor
public class PurDet implements Serializable {

    @EmbeddedId
    private PurDetId id;

    private Integer seqNo;
    private String projectNo;
    private String itemNo;
    private Integer itemType;
    private String partNo;
    private String loc;
    private String uom;
    private String stockStatus;
    private String orderNo;
    private Date dueDate;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "UNIT_PRICE", precision = 16, scale = 4)
    private BigDecimal unitPrice;

    private Date rlseDate;

    @Column(name = "RLSE_QTY", precision = 16, scale = 4)
    private BigDecimal rlseQty;

    private Date recdDate;

    @Column(name = "RECD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    @Column(name = "RECD_PRICE", precision = 16, scale = 4)
    private BigDecimal recdPrice;

    @Column(name = "RESV_QTY", precision = 16, scale = 4)
    private BigDecimal resvQty;

    private String invUom;

    @Column(name = "STD_PACK_QTY", precision = 12, scale = 6)
    private BigDecimal stdPackQty;

    @Column(name = "STD_MATERIAL", precision = 16, scale = 4)
    private BigDecimal stdMaterial;

    @Column(name = "FREIGHT", precision = 16, scale = 4)
    private BigDecimal freight;

    @Column(name = "HANDLING", precision = 16, scale = 4)
    private BigDecimal handling;

    @Column(name = "CUSTOM_DUTY", precision = 16, scale = 4)
    private BigDecimal customDuty;

    private String anyDiscount;

    @Column(name = "DISCOUNT_AMOUNT", precision = 16, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "DISCOUNT_PERCENT", precision = 8, scale = 4)
    private BigDecimal discountPercent;

    private Date mbiDate;
    private String buyFlag;
    private String advStatus;
    private String remarks;

    @Column(name = "AP_RECD_QTY", precision = 16, scale = 4)
    private BigDecimal apRecdQty;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "poNo", referencedColumnName = "poNo", insertable = false, updatable = false)
    })
    private Pur pur;
}
