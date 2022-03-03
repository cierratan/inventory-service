package com.sunright.inventory.entity.bombypj;

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
public class BombypjId extends BaseIdEntity {

    private String projectNo;
    private String orderNo;
    private String assemblyNo;
    private String component;
    private String alternate;
}
