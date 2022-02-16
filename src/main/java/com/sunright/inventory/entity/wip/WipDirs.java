package com.sunright.inventory.entity.wip;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "WIPDIRS")
@Data
@NoArgsConstructor
public class WipDirs implements Serializable {

    @EmbeddedId
    private WipDirsId id;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "wipDirs", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WipDirsDetail> wipDirsDetails;
}
