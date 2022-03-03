package com.sunright.inventory.entity.itemeql;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.util.Date;

@Entity(name = "ITEMEQL")
@Data
@NoArgsConstructor
public class Itemeql {

    @EmbeddedId
    private ItemeqlId id;

    private String rpcNo;
    private Date entryDate;
    private String entryUser;
    private String openClose;
    private Date closeDate;
    private String customerCode;
    private String customerGroup;
    private String mncName;
}
