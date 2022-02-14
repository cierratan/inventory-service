package com.sunright.inventory.entity.sale;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity(name = "SALE")
@Data
@NoArgsConstructor
public class Sale {

    @EmbeddedId
    private SaleId id;

    private String categoryCode;
    private String categorySubCode;
    private String customerCode;
    private String orderRef;
    private Date rlseDate;
    private String customerPoInd;
    private String customerPoNo;
    private Date customerPoRecdDate;
    private String quotationNo;
    private String salesmanCode;
    private Integer paymentTerm;
    private String paymentDesc;
    private String shippingTerm;
    private String divCode;
    private String deptCode;
    private String Address1;
    private String Address2;
    private String Address3;
    private String Address4;
    private String coqCatCode;
    private String coqReasonCode;
    private String coqDivCode;
    private String coqDeptCode;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "sale")
    private Set<SaleDetail> saleDetails;
}
