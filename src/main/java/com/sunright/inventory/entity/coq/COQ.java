package com.sunright.inventory.entity.coq;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity(name = "COQ")
@Data
@NoArgsConstructor
public class COQ implements Serializable {

    @EmbeddedId
    private COQId id;

    private String docmType;
    private String catCode;
    private String requestor;
    private String projectNoSub;
    private String sraNo;
    private String status;
    private String entryUser;
    private Date entryDate;
    private String remarks;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coq", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<COQDetail> coqDetails;
}
