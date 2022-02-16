package com.sunright.inventory.entity.wip;

import com.sunright.inventory.entity.pur.Pur;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "WIPPROJ_DET")
@Data
@NoArgsConstructor
public class WipProjDetail implements Serializable {

    @EmbeddedId
    private WipProjDetailId id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "projectNoSub", referencedColumnName = "projectNoSub", insertable = false, updatable = false),
            @JoinColumn(name = "projectNo", referencedColumnName = "projectNo", insertable = false, updatable = false),
            @JoinColumn(name = "orderNo", referencedColumnName = "orderNo", insertable = false, updatable = false)
    })
    private WipProj wipProj;
}
