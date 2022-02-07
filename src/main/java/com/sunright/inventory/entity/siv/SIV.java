package com.sunright.inventory.entity.siv;

import com.sunright.inventory.entity.InvBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "SIV")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SIV extends InvBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String subType;
    private String sivNo;
    private String projectNo;
    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    @Column(name = "STATUS_1")
    private String statuz;

    private String entryTime;
    private String tranType;
    private String docmNo;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "siv")
    private Set<SIVDetail> sivDetails;
}
