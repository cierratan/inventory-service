package com.sunright.inventory.entity.wip;

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

    private String type;
    private String subType;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "wipProj", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WipProjDetail> wipProjDetails;
}
