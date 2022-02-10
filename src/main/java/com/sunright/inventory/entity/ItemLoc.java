package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

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

    @Column(name = "PICKED_QTY", precision = 16, scale = 4)
    private BigDecimal pickedQty;

    @Column(name = "PRODN_RESV", precision = 16, scale = 4)
    private BigDecimal prodnResv;

    @Column(name = "RPC_RESV", precision = 16, scale = 4)
    private BigDecimal rpcResv;

    @Column(name = "MRV_RESV", precision = 16, scale = 4)
    private BigDecimal mrvResv;

    @Column(name = "YTD_PROD", precision = 16, scale = 4)
    private BigDecimal ytdProd;

    @Column(name = "YTD_ISSUE", precision = 16, scale = 4)
    private BigDecimal ytdIssue;

    @Column(name = "STD_MATERIAL", precision = 16, scale = 4)
    private BigDecimal stdMaterial;

    private Date lastTranDate;
}
