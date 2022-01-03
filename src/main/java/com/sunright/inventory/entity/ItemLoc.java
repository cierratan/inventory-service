package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;
import java.math.BigDecimal;

@Entity(name = "ITEMLOC")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ItemLoc extends BaseEntity {
    @EmbeddedId
    private ItemLocId id;

    @Version
    private Long version;

    private String loc;
    private String partNo;
    private String description;

    @Column(name = "QOH", precision = 16, scale = 4)
    private BigDecimal qoh;

    private String categoryCode;
    private String categorySubCode;
}
