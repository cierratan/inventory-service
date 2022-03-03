package com.sunright.inventory.entity.bom;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "BOM")
@Data
@NoArgsConstructor
public class Bom implements Serializable {

    @EmbeddedId
    private BomId id;

    private String component;

    @Column(name = "QTY_PER_ASSM", precision = 16, scale = 4)
    private BigDecimal qtyPerAssm;

    @Column(name = "MATL_COST", precision = 16, scale = 4)
    private BigDecimal matlCost;

    @Column(name = "LABR_COST", precision = 16, scale = 4)
    private BigDecimal labrCost;

    private Date effectiveDate;
    private Date obsoleteDate;
    private String engineer;
    private String reference;
    private String mrpStatus;
    private Date revDate;
    private Long revNo;
    private String revRef;
    private String entryUser;
    private Date entryDate;
    private String remarks;
    private String eqvStatus;
}
