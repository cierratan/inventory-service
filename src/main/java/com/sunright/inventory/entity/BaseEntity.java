package com.sunright.inventory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {
    private String companyCode;
    private Integer plantNo;
}
