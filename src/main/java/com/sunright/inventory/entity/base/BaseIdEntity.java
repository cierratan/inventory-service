package com.sunright.inventory.entity.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Embeddable
@MappedSuperclass
@Data
@NoArgsConstructor
public class BaseIdEntity implements Serializable {
    private String companyCode;
    private Integer plantNo;
}
