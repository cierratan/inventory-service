package com.sunright.inventory.entity.uom;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity(name = "UOM")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UOM extends BaseEntity {
    @EmbeddedId
    private UOMId id;

    private String fromDescription;
    private String toDescription;

    @Column(name = "UOM_FACTOR", precision = 16, scale = 6)
    private BigDecimal uomFactor;

    @Version
    private Long version;
}
