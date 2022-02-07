package com.sunright.inventory.entity.itembatc;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "ITEMBATC")
@Data
@NoArgsConstructor
public class ItemBatc {

    @EmbeddedId
    private ItemBatcId id;

    private String grnNo;
    private Integer grnSeq;

    @Column(name = "QOH", precision = 16, scale = 4)
    private BigDecimal qoh;

    @Column(name = "ORI_QOH", precision = 16, scale = 4)
    private BigDecimal oriQoh;

    private Integer dateCode;
    private String poNo;
    private Integer poRecSeq;
}
