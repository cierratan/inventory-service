package com.sunright.inventory.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "GRNDET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnDet {

    @EmbeddedId
    private GrnId grnId;

    private Integer seqNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String projectNo;
    private String poNo;
    private Integer poRecSeq;
    private String doNo;
    private String sivNo;
    private String uom;
    private Date recdDate;

    @Column(name = "RECD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    @Column(name = "RECD_PRICE", precision = 16, scale = 4)
    private BigDecimal recdPrice;

    @Column(name = "PO_PRICE", precision = 16, scale = 4)
    private BigDecimal poPrice;

    @Column(name = "ISSUED_QTY", precision = 16, scale = 4)
    private BigDecimal issuedQty;

    @Column(name = "LABEL_QTY", precision = 16, scale = 4)
    private BigDecimal labelQty;

    @Column(name = "STD_PACK_QTY", precision = 12, scale = 6)
    private BigDecimal stdPackQty;

    @Column(name = "AP_RECD_QTY", precision = 16, scale = 4)
    private BigDecimal apRecdQty;

    private String remarks;

    //@JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns({@JoinColumn(name = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "grnNo", insertable = false, updatable = false),
            @JoinColumn(name = "subType", insertable = false, updatable = false)
    })
    private Grn grn;
}
