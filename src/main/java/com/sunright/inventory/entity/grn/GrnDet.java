package com.sunright.inventory.entity.grn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
    private int plantNo;
    private String grnNo;
    private String subType;

    private int seqNo;
    private String itemNo;
    private String partNo;
    private String loc;
    private int itemType;
    private String projectNo;
    private String poNo;
    private int poRecSeq;
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
    @JsonIgnore
    private Grn grn;
}
