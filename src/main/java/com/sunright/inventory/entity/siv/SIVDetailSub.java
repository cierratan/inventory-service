package com.sunright.inventory.entity.siv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "SIVDET_SUB")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SIVDetailSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyCode;
    private Integer plantNo;
    private String subType;
    private String sivNo;
    private int seqNo;
    private Integer detailSeq;
    private String itemNo;
    private String saleType;
    private String docmNo;

    @Column(name = "ISSUED_QTY", precision = 16, scale = 4)
    private BigDecimal issuedQty;

    @ManyToOne
    @JoinColumn(name = "SIVDET_ID", nullable = false)
    private SIVDetail sivDetail;
}
