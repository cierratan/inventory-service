package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseIdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemCatId extends BaseIdEntity {
     private String categoryCode;
     private String categorySubCode;
     private String categoryGroup;
}
