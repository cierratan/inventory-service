package com.sunright.inventory.entity.grn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity(name = "GRN")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grn {

    @EmbeddedId
    private GrnId grnId;

    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    private Date recdDate;
    private String status;
    private String entryUser;
    private Date entryDate;
    private Date closedDate;
    private String docmType;
    private String requestor;
    private String approver;
    private Date approvalDate;
    private String reqSubmitNo;
    private Date reqSubmitDate;

    //@JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "grn", cascade = CascadeType.ALL)
    private List<GrnDet> grnDetList;
}
