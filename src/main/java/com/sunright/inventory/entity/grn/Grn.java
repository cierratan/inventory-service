package com.sunright.inventory.entity.grn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sunright.inventory.entity.base.InvBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Entity(name = "GRN")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Grn extends InvBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String grnNo;
    private String subType;
    private String poNo;
    private String doNo;
    private String supplierCode;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    private Date recdDate;

    @Column(name = "STATUS_1")
    private String statuz;

    private Date closedDate;
    private String docmType;
    private String requestor;
    private String approver;
    private Date approvalDate;
    private String reqSubmitNo;
    private Date reqSubmitDate;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "grn", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GrnDet> grnDetails;
}
