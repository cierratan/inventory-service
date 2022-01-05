package com.sunright.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "DOCM_NO")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DocmNo extends BaseEntity {

    @EmbeddedId
    private DocmNoId id;

    /*@Version
    private Long version;*/

    private String description;
    private Integer lastGeneratedNo;
    private String prefix;
    private String postfix;
}
