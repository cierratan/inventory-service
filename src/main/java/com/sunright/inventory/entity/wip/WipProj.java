package com.sunright.inventory.entity.wip;

import com.sunright.inventory.entity.pur.PurDet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "WIPPROJ")
@Data
@NoArgsConstructor
public class WipProj implements Serializable {

    @EmbeddedId
    private WipProjId id;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "wipProj", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WipProjDetail> wipProjDetails;
}
