package com.sunright.inventory.entity.coq;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "COQ_REASONS")
@Data
@NoArgsConstructor
public class COQReasons implements Serializable {

    @EmbeddedId
    private COQReasonsId id;

    private String docmDesc;
    private String catDesc;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coqReasons", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<COQReasonsDetail> coqReasonsDetails;
}
