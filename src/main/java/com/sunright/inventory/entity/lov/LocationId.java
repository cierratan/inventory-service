package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationId extends BaseEntity {
    private String loc;
}
