package com.sunright.inventory.entity.mrv;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "MRVDET")
@Data
@NoArgsConstructor
public class MRVDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private Integer plantNo;
    private String mrvNo;
    private int seqNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private Integer itemType;
    private Long batchNo;
    private String projectNo;
    private String sivNo;
    private String uom;

    @Column(name = "RECD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    @Column(name = "RECD_PRICE", precision = 16, scale = 4)
    private BigDecimal recdPrice;

    private String docmNo;
    private String tranType;
    private String saleType;
    private String replace;
    private String msrStatus;

    @Column(name = "LABEL_QTY", precision = 16, scale = 4)
    private BigDecimal labelQty;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "MRV_ID", nullable = false)
    private MRV mrv;
}
