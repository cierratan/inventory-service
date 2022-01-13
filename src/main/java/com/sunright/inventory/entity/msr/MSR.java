package com.sunright.inventory.entity.msr;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "MSR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MSR extends BaseEntity {

    @EmbeddedId
    private MSRId id;

    @Version
    private Long version;

    private String supplierCode;
    private String docmNo;
    private String batchId;
    private String originator;
    private String mrvNo;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(referencedColumnName = "companyCode", updatable = false),
            @JoinColumn(referencedColumnName = "plantNo", updatable = false),
            @JoinColumn(referencedColumnName = "msrNo", updatable = false)
    })
    private Set<MSRDetail> msrDetails;
}
