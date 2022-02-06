package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "INAUDIT")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InAudit extends InvBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Long itemlocId;
    private String itemNo;
    private Date tranDate;
    private String tranTime;
    private String loc;
    private String tranType;
    private String docmNo;

    @Column(name = "OUT_QTY", precision = 16, scale = 4)
    private BigDecimal outQty;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "BAL_QTY", precision = 16, scale = 4)
    private BigDecimal balQty;

    private String projectNo;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    @Column(name = "ACTUAL_COST", precision = 16, scale = 4)
    private BigDecimal actualCost;

    private String grnNo;
    private String poNo;
    private String doNo;
    private String remarks;
}
