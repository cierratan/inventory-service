package com.sunright.inventory.entity.lov;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
public class CodeDescId implements Serializable {
    private String type;
    private String subType;
}
