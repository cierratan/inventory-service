package com.sunright.inventory.entity.sfcwip;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Data
public class SfcWipId implements Serializable {

    private String projectNoSub;
    private String pcbPartNo;
}
