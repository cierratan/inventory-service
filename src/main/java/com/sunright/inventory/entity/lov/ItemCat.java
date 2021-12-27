package com.sunright.inventory.entity.lov;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "ITEMCAT")
@Data
@NoArgsConstructor
public class ItemCat {

    @EmbeddedId
    private ItemCatId id;

    private String description;
    private String subDescription;
}
