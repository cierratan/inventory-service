package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity(name = "INVCTL")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvCtl extends BaseEntity {

    @EmbeddedId
    private BaseIdEntity id;

    @Version
    private Long version;

    @Column(name = "STOCK_DEPN", precision = 5, scale = 2)
    private BigDecimal stockDepn;

    @Column(name = "PROV_AGE", precision = 5, scale = 2)
    private BigDecimal provAge;
}
