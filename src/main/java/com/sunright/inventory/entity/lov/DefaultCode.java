package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity(name = "DEFAULT_CODE")
@Data
@NoArgsConstructor
public class DefaultCode extends BaseEntity {

    @EmbeddedId
    private DefaultCodeId id;

    @Version
    private Long version;

    private String dataBlock;
    private String codeName;
    private String dbStatus;
    private String defaultValue;
    private String description;
}
