package com.sunright.inventory.entity.mrv;

import com.sunright.inventory.entity.base.InvBaseEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "MRV")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MRV extends InvBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String mrvNo;

    @Column(name = "STATUS_1")
    private String statuz;

    private String currencyCode;

    @Column(name = "CURRENCY_RATE", precision = 10, scale = 6)
    private BigDecimal currencyRate;

    private String entryTime;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "mrv", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MRVDetail> mrvDetails;
}
