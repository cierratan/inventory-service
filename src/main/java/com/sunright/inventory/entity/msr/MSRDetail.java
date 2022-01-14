package com.sunright.inventory.entity.msr;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "MSRDET")
@Data
@NoArgsConstructor
public class MSRDetail {

    @EmbeddedId
    private MSRDetailId id;

    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String batchNo;
    private String projectNo;
    private String grnType;
    private String grnNo;
    private Integer grndetSeqNo;

    @Column(name = "GRNDET_RECD_QTY", precision = 16, scale = 4)
    private BigDecimal grndetRecdQty;

    private String supplierRetn;

    @Column(name = "RETN_QTY", precision = 16, scale = 4)
    private BigDecimal retnQty;

    @Column(name = "RETN_PRICE", precision = 16, scale = 4)
    private BigDecimal retnPrice;

    private String retnType;
    private String retnAction;
    private String uom;

    @Column(name = "RECD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    @Column(name = "RECD_PRICE", precision = 16, scale = 4)
    private BigDecimal recdPrice;

    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    private String mrvNo;
    private String remarks;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "msrNo", referencedColumnName = "msrNo", nullable = false, insertable = false, updatable = false)
    })
    private MSR msr;
}
