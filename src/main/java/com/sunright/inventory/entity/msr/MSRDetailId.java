package com.sunright.inventory.entity.msr;

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
public class MSRDetailId extends BaseIdEntity {
    private String msrNo;
    private int seqNo;
}
