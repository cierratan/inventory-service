package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseEntity;
import com.sunright.inventory.entity.enums.MRPStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "ITEMCAT")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemCat extends BaseEntity {

    @EmbeddedId
    private ItemCatId id;

    @Version
    private Long version;

    private String description;
    private String subDescription;

    @Enumerated(EnumType.ORDINAL)
    private MRPStatus mrpStatus;

    @Column(name = "DESIGN_QTYA", precision = 16, scale = 4)
    private BigDecimal designQtya;

    @Column(name = "DESIGN_QTYB", precision = 16, scale = 4)
    private BigDecimal designQtyb;

    @Column(name = "DESIGN_QTYC", precision = 16, scale = 4)
    private BigDecimal designQtyc;

    @Column(name = "DESIGN_QTYD", precision = 16, scale = 4)
    private BigDecimal designQtyd;

    private Integer mifA;
    private Integer mifB;
    private Integer mifC;
    private Integer mifD;
}
