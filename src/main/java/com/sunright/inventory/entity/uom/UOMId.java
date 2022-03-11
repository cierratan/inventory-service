package com.sunright.inventory.entity.uom;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class UOMId implements Serializable {
    private String uomFrom;
    private String uomTo;
}
