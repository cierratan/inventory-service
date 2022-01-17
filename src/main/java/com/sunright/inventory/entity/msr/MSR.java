package com.sunright.inventory.entity.msr;

import com.sunright.inventory.entity.InvBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "MSR")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MSR extends InvBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String msrNo;
    private String supplierCode;
    private String docmNo;
    private String batchId;
    private String originator;
    private String mrvNo;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "msr")
    private Set<MSRDetail> msrDetails;
}
