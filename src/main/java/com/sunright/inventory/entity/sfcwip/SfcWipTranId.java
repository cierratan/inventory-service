package com.sunright.inventory.entity.sfcwip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Data
public class SfcWipTranId implements Serializable {

    private String productId;
}
