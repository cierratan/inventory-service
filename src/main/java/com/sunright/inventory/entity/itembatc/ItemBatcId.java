package com.sunright.inventory.entity.itembatc;

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
public class ItemBatcId extends BaseIdEntity {
     private String itemNo;
     private String loc;
     private Long batchNo;
}
