package com.sunright.inventory.entity.coq;

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
public class COQDetailSubId extends BaseIdEntity {
    private String docmNo;
    private Integer detRecSeq;
    private Integer seqNo;
}
