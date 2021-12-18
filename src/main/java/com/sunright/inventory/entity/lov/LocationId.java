package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseEntity;
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
public class LocationId extends BaseEntity {
    private String loc;
}
