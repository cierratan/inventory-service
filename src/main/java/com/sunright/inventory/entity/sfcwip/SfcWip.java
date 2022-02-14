package com.sunright.inventory.entity.sfcwip;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "SFC_WIP")
@Data
@NoArgsConstructor
public class SfcWip implements Serializable {

    @EmbeddedId
    private SfcWipId id;

    @Column(name = "PCB_QTY", precision = 16, scale = 4)
    private BigDecimal pcbQty;

    private String flowId;
    private String status;
    private Integer ctnQty;
    private String remarks;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "sfcWip", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SfcWipTran> sfcWipTrans;
}
