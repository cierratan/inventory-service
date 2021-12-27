package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity(name = "DEFAULT_CODE_DET")
@Data
@NoArgsConstructor
public class DefaultCodeDetail extends BaseEntity {
    @EmbeddedId
    private DefaultCodeDetailId id;

    @Version
    private Long version;

    private String codeValue;
    private String codeDesc;
    private String remarks;

}
