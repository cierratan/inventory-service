package com.sunright.inventory.entity.lov;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = "CODE_DESC")
@Data
@NoArgsConstructor
public class CodeDesc implements Serializable {
    @EmbeddedId
    private CodeDescId id;

    private String description;
    private String subtypeDesc;
}
