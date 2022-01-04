package com.sunright.inventory.entity.grn;

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
//@EqualsAndHashCode(callSuper = true)
public class GrnId extends BaseIdEntity {

    private String grnNo;
    private String subType;
}
