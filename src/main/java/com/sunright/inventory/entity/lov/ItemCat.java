package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.base.InvBaseEntity;
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
public class ItemCat extends InvBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String categoryCode;
    private String categorySubCode;
    private String categoryGroup;

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

    @Column(name = "MIF_A")
    private Integer mifA;

    @Column(name = "MIF_B")
    private Integer mifB;

    @Column(name = "MIF_C")
    private Integer mifC;

    @Column(name = "MIF_D")
    private Integer mifD;
}
