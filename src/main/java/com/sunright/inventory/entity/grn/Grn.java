package com.sunright.inventory.entity.grn;

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
    private GrnId ids;

    @Version
    private Long version;

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
    private List<GrnDet> grnDetList;
}
