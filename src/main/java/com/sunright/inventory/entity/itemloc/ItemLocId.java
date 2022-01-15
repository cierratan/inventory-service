package com.sunright.inventory.entity.itemloc;

import com.sunright.inventory.entity.BaseIdEntity;
import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
public class ItemLocId extends BaseIdEntity {
    private String itemNo;
}
