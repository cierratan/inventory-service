package com.sunright.inventory.entity.itemloc;

import com.sunright.inventory.entity.base.InvBaseEntity;
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

    @Column(name = "BATCH_NO", precision = 8)
    private BigDecimal batchNo;

    @Column(name = "COST_VARIANCE", precision = 16, scale = 4)
    private BigDecimal costVariance;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "BALBF_QTY", precision = 16, scale = 4)
    private BigDecimal balbfQty;

    @Column(name = "YTD_RECEIPT", precision = 16, scale = 4)
    private BigDecimal ytdReceipt;

    @Column(name = "LAST_PUR_PRICE", precision = 16, scale = 4)
    private BigDecimal lastPurPrice;

    private Date lastTranDate;
}
