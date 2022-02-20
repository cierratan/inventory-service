package com.sunright.inventory.entity.pr;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "PR")
@Data
@NoArgsConstructor
public class PR implements Serializable {

    @EmbeddedId
    private PRId id;

    private String remarks;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pr", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PRDetail> prDetails;
}
