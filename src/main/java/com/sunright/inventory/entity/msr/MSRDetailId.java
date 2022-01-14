package com.sunright.inventory.entity.msr;

import com.sunright.inventory.entity.BaseIdEntity;
import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
public class MSRDetailId extends BaseIdEntity {
    private String msrNo;
    private int seqNo;
}
