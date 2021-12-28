package com.sunright.inventory.entity.lov;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "CODE_MAP")
@Data
@NoArgsConstructor
public class CodeMap {

    @EmbeddedId
    private CodeMapId id;

    private String disallow;
    private String remarks;
}
