package com.sunright.inventory.entity.lov;

import com.sunright.inventory.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity(name = "DEFAULT_CODE_DET")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DefaultCodeDetail extends BaseEntity {
    @EmbeddedId
    private DefaultCodeDetailId id;

    @Version
    private Long version;

    private String codeValue;
    private String codeDesc;
    private String remarks;

}
