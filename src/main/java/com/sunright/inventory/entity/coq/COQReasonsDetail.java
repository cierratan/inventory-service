package com.sunright.inventory.entity.coq;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "COQ_REASONS_DET")
@Data
@NoArgsConstructor
public class COQReasonsDetail implements Serializable {

    @EmbeddedId
    private COQReasonsDetailId id;

    private String reasonDesc;
    private String status;
    private String entryUser;
    private Date entryDate;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "docmType", referencedColumnName = "docmType", insertable = false, updatable = false),
            @JoinColumn(name = "catCode", referencedColumnName = "catCode", insertable = false, updatable = false)
    })
    private COQReasons coqReasons;
}
