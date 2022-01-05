package com.sunright.inventory.entity.grn;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity(name = "GRN")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Grn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private GrnId grnId;

    @Version
    private Long version;

    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    private Date recdDate;

    @Column(name = "STATUS_1", insertable = false, updatable = false)
    private String statuz;

    private String entryUser;
    private Date entryDate;
    private Date closedDate;
    private String docmType;
    private String requestor;
    private String approver;
    private Date approvalDate;
    private String reqSubmitNo;
    private Date reqSubmitDate;

    @JsonManagedReference(value = "grnDetId")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "grn", cascade = CascadeType.ALL)
    private List<GrnDet> grnDetList;
}
