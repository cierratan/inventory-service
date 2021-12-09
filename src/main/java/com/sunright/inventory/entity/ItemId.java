package com.sunright.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemId implements Serializable {
    private String companyCode;
    private Integer plantNo;
    private String itemNo;
}
