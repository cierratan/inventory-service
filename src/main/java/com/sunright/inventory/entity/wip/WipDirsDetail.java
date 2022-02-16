package com.sunright.inventory.entity.wip;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "WIPDIRS_DET")
@Data
@NoArgsConstructor
public class WipDirsDetail implements Serializable {

    @EmbeddedId
    private WipDirsDetailId id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "companyCode", referencedColumnName = "companyCode", insertable = false, updatable = false),
            @JoinColumn(name = "plantNo", referencedColumnName = "plantNo", insertable = false, updatable = false),
            @JoinColumn(name = "orderNo", referencedColumnName = "orderNo", insertable = false, updatable = false),
            @JoinColumn(name = "itemNo", referencedColumnName = "itemNo", insertable = false, updatable = false)
    })
    private WipDirs wipDirs;
}
