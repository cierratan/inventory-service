package com.sunright.inventory.entity.wip;

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
public class WipDirsDetailId extends BaseIdEntity {
    private String orderNo;
    private String itemNo;
    private int monthSeq;
}
