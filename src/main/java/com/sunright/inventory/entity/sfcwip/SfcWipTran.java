package com.sunright.inventory.entity.sfcwip;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "SFC_WIP_TRAN")
@Data
@NoArgsConstructor
public class SfcWipTran implements Serializable {

    @EmbeddedId
    private SfcWipTranId id;

    private String projectNoSub;
    private String pcbPartNo;

    private String projectCycle;
    private String tranId;
    private String flowId;
    private Integer seqNo;
    private Integer levelNo;
    private Date timeIn;
    private Date timeOut;
    private String status;
    private String remarks;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "projectNoSub", referencedColumnName = "projectNoSub", insertable = false, updatable = false),
            @JoinColumn(name = "pcbPartNo", referencedColumnName = "pcbPartNo", insertable = false, updatable = false)
    })
    private SfcWip sfcWip;
}
