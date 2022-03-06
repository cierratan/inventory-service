package com.sunright.inventory.entity.grn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "GRNDET")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GrnDet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private Integer plantNo;
    private String grnNo;
    private String subType;

    private int seqNo;
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

    @ManyToOne
    @JoinColumn(name = "GRN_ID", nullable = false)
    private Grn grn;
}
