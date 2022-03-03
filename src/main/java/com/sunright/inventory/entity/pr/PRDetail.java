package com.sunright.inventory.entity.pr;

import com.sunright.inventory.entity.pur.Pur;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "PRDET")
@Data
@NoArgsConstructor
public class PRDetail implements Serializable {

    @EmbeddedId
    private PRDetailId id;

    private Integer seqNo;
    private Integer itemType;
    private String itemNo;
    private String partNo;
    private String description;
    private Date dueDate;
    private String source;
    private String uom;
    private String loc;

    @Column(name = "QTY", precision = 16, scale = 4)
    private BigDecimal qty;

    @Column(name = "UNIT_PRICE", precision = 16, scale = 4)
    private BigDecimal unitPrice;

    private String reasonCode;
    private String reasonDesc;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "docmNo", referencedColumnName = "docmNo", insertable = false, updatable = false)
    })
    private PR pr;
}
