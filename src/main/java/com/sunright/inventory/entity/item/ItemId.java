package com.sunright.inventory.entity.item;

import com.sunright.inventory.entity.BaseIdEntity;
import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
public class ItemId extends BaseIdEntity {
    private String itemNo;
}
