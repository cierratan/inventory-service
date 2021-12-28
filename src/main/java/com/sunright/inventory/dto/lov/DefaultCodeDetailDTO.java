package com.sunright.inventory.dto.lov;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class DefaultCodeDetailDTO extends BaseDTO {
    private String defaultCode;
    private String codeValue;
    private String codeDesc;
}
