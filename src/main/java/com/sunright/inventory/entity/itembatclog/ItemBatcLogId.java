package com.sunright.inventory.entity.itembatclog;

import com.sunright.inventory.entity.base.BaseIdEntity;
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
public class ItemBatcLogId extends BaseIdEntity {
    private String itemNo;
    private String loc;
    private Long batchNo;
    private String sivNo;
}
