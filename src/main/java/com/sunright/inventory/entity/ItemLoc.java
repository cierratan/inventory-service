package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "ITEMLOC")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemLoc extends InvBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Long itemId;
    private String itemNo;
    private String loc;
    private String partNo;
    private String description;

    @Column(name = "QOH", precision = 16, scale = 4)
    private BigDecimal qoh;

    private String categoryCode;
    private String categorySubCode;
}