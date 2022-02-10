package com.sunright.inventory.entity.sale;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "SALEDET")
@Data
@NoArgsConstructor
public class SaleDetail {

    @EmbeddedId
    private SaleDetailId id;

    private String projectNoSub;
    private String productType;
    private String productSubType;
    private String productDesc;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "orderNo", referencedColumnName = "orderNo", insertable = false, updatable = false)
    })
    private Sale sale;
}
