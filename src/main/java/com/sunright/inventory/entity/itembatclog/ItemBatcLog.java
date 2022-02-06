package com.sunright.inventory.entity.itembatclog;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "ITEMBATC_LOG")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemBatcLog extends BaseEntity {

    @EmbeddedId
    private ItemBatcLogId id;

    @Column(name = "SIV_QTY", precision = 16, scale = 4)
    private BigDecimal sivQty;

    private Integer dateCode;
    private String poNo;
    private Integer poRecSeq;
    private String grnNo;
    private Integer grnSeq;

    @Column(name = "GRN_QTY", precision = 16, scale = 4)
    private BigDecimal grnQty;
}
