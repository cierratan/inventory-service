package com.sunright.inventory.entity.itemeql;

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
public class ItemeqlId extends BaseIdEntity {
    private String itemNo;
    private String alternate;
    private String assemblyNo;
}
