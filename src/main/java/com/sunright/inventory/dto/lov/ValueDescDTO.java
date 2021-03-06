package com.sunright.inventory.dto.lov;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class ValueDescDTO {
    private String codeValue;
    private String codeDesc;
}
