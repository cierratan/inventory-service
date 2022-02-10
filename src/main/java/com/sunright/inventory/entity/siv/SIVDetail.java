package com.sunright.inventory.entity.siv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "SIVDET")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SIVDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private Integer plantNo;
    private String subType;
    private String sivNo;
    private int seqNo;

    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private String uom;
    private Integer batchNo;
    private String poNo;
    private String prNo;
    private String grnNo;
    private int grnDetSeqNo;

    @Column(name = "GRNDET_RECD_PRICE", precision = 16, scale = 4)
    private BigDecimal grnDetRecdPrice;

    @Column(name = "ISSUED_QTY", precision = 16, scale = 4)
    private BigDecimal issuedQty;

    @Column(name = "ISSUED_PRICE", precision = 16, scale = 4)
    private BigDecimal issuedPrice;

    private String saleType;
    private String docmNo;

    @Column(name = "EXTRA_QTY", precision = 16, scale = 4)
    private BigDecimal extraQty;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "SIV_ID", nullable = false)
    private SIV siv;
}
