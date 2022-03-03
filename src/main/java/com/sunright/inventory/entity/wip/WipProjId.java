package com.sunright.inventory.entity.wip;

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
public class WipProjId extends BaseIdEntity {
    private String projectNoSub;
    private String projectNo;
    private String orderNo;
}
