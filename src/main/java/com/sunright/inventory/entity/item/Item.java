package com.sunright.inventory.entity.item;

import com.sunright.inventory.entity.base.InvBaseEntity;
import com.sunright.inventory.entity.enums.Closure;
import com.sunright.inventory.entity.enums.Obsolete;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "ITEM")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Item extends InvBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String itemNo;
    private String loc;
    private String partNo;
    private String description;
    private String categoryCode;
    private String categorySubCode;
    private String categoryGroup;
    private String manufacturer;
    private String uom;
    private String drawing;
    private String issueNo;
    private String specNo;
    private String qa;
    private String rev;

    @Column(name = "class")
    private String clazz;

    private String source;
    private String orderPolicy;
    private String alternate;
    private String productGroup;
    private String buyer;
    private String supplierCode;
    private String accountCode;
    private String countCode;
    private String divCode;
    private String deptCode;
    private String remarks;

    @Column(name = "QOH", precision = 16, scale = 4)
    private BigDecimal qoh;

    @Column(name = "BALBF_QTY", precision = 16, scale = 4)
    private BigDecimal balbfQty;

    @Column(name = "LAST_BALBF_QTY", precision = 16, scale = 4)
    private BigDecimal lastBalbfQty;

    @Column(name = "PRICEA", precision = 16, scale = 4)
    private BigDecimal pricea;

    @Column(name = "PRICEB", precision = 16, scale = 4)
    private BigDecimal priceb;

    @Column(name = "PRICEC", precision = 16, scale = 4)
    private BigDecimal pricec;

    @Column(name = "REORDER", precision = 16, scale = 4)
    private BigDecimal reorder;

    @Column(name = "LEADTIME", precision = 3, scale = 0)
    private BigDecimal leadtime;

    @Column(name = "LOT", precision = 16, scale = 4)
    private BigDecimal lot;

    @Column(name = "YTD_PURCH", precision = 16, scale = 4)
    private BigDecimal ytdPurch;

    @Column(name = "YTD_PROD", precision = 16, scale = 4)
    private BigDecimal ytdProd;

    @Column(name = "YTD_SALE", precision = 16, scale = 4)
    private BigDecimal ytdSale;

    @Column(name = "YTD_ISSUE", precision = 16, scale = 4)
    private BigDecimal ytdIssue;

    @Column(name = "YTD_RECEIPT", precision = 16, scale = 4)
    private BigDecimal ytdReceipt;

    @Column(name = "OBSOLETE_QTY", precision = 16, scale = 4)
    private BigDecimal obsoleteQty;

    @Column(name = "BATCH_NO", precision = 8)
    private BigDecimal batchNo;

    @Column(name = "ORDER_QTY", precision = 16, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "PICKED_QTY", precision = 16, scale = 4)
    private BigDecimal pickedQty;

    @Column(name = "PRODN_RESV", precision = 16, scale = 4)
    private BigDecimal prodnResv;

    @Column(name = "SALES_RESV", precision = 16, scale = 4)
    private BigDecimal salesResv;

    @Column(name = "ADV_PR_RESV", precision = 16, scale = 4)
    private BigDecimal advPrResv;

    @Column(name = "SAFETY_STOCK", precision = 16, scale = 4)
    private BigDecimal safetyStock;

    @Column(name = "STD_SETUP", precision = 16, scale = 4)
    private BigDecimal stdSetup;

    @Column(name = "STD_MATERIAL", precision = 16, scale = 4)
    private BigDecimal stdMaterial;

    @Column(name = "STD_LABOR", precision = 16, scale = 4)
    private BigDecimal stdLabor;

    @Column(name = "STD_BURDEN", precision = 16, scale = 4)
    private BigDecimal stdBurden;

    @Column(name = "STD_SUBCONTRACT", precision = 16, scale = 4)
    private BigDecimal stdSubcontract;

    @Column(name = "STD_MACHINE", precision = 16, scale = 4)
    private BigDecimal stdMachine;

    @Column(name = "FREIGHT", precision = 16, scale = 4)
    private BigDecimal freight;

    @Column(name = "HANDLING", precision = 16, scale = 4)
    private BigDecimal handling;

    @Column(name = "CUSTOM_DUTY", precision = 16, scale = 4)
    private BigDecimal customDuty;

    @Column(name = "LAST_PUR_PRICE", precision = 16, scale = 4)
    private BigDecimal lastPurPrice;

    private Date lastPurDate;
    private Date lastTranDate;
    private Date lastCountDate;

    @Column(name = "COST_VARIANCE", precision = 16, scale = 4)
    private BigDecimal costVariance;

    private String qcFlag;

    @Column(name = "COUNT_QOH", precision = 16, scale = 4)
    private BigDecimal countQoh;

    private String countTag;
    private String countUpdInd;
    private String dimension;

    @Column(name = "ROHS_STATUS")
    private String strRohsStatus;

    @Column(name = "RPC_RESV", precision = 16, scale = 4)
    private BigDecimal rpcResv;

    @Column(name = "MRV_RESV", precision = 16, scale = 4)
    private BigDecimal mrvResv;

    private String mslCode;

    @Enumerated(EnumType.STRING)
    private Closure openClose;
    private Date closeDate;

    @Enumerated(EnumType.STRING)
    private Obsolete obsoleteCode;
    private String obsoleteItem;
    private Date obsoleteDate;

    private String refUrl;
    private String requestor;
    private String storageShelf;
}
