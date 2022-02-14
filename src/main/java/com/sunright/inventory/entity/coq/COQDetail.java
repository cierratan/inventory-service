package com.sunright.inventory.entity.coq;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Entity(name = "COQ_DET")
@Data
@NoArgsConstructor
public class COQDetail implements Serializable {

    @EmbeddedId
    private COQDetailId id;

    private Integer seqNo;
    private Integer itemType;
    private String partNo;
    private String itemNo;
    private String assemblyNo;
    private String description;

    @Column(name = "DOCM_QTY", precision = 16, scale = 4)
    private BigDecimal docmQty;

    private String reasonCode;
    private String reasonDesc;
    private String divCode;
    private String deptCode;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "docmNo", referencedColumnName = "docmNo", insertable = false, updatable = false)
    })
    private COQ coq;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coqDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<COQDetailSub> coqDetailsSub;
}
