package com.sunright.inventory.entity.bombypj;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "BOMBYPJ_DET")
@Data
@NoArgsConstructor
public class BombypjDet {

    @EmbeddedId
    private BombypjId id;

    private String poNo;
    private Integer seqNo;

    @Column(name = "RESV_QTY", precision = 16, scale = 4)
    private BigDecimal resvQty;

    @Column(name = "ACCUM_RECD_QTY", precision = 16, scale = 4)
    private BigDecimal accumRecdQty;

    @Column(name = "RECD_QTY", precision = 16, scale = 4)
    private BigDecimal recdQty;

    private String status;
    private String grnNo;
}
