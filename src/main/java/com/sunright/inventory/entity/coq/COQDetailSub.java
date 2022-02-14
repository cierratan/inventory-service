package com.sunright.inventory.entity.coq;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name = "COQ_DET_SUB")
@Data
@NoArgsConstructor
public class COQDetailSub implements Serializable {

    @EmbeddedId
    private COQDetailSubId id;

    private String sivNo;

    @Column(name = "QTY", precision = 16, scale = 4)
    private BigDecimal qty;

    private String poNo;

    @Column(name = "UNIT_PRICE", precision = 16, scale = 4)
    private BigDecimal unitPrice;

    private Date entryDate;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "docmNo", referencedColumnName = "docmNo", insertable = false, updatable = false),
            @JoinColumn(name = "recSeq", referencedColumnName = "recSeq", insertable = false, updatable = false)
    })
    private COQDetail coqDetail;
}
