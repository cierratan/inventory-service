package com.sunright.inventory.dto.lov;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@Builder
public class CodeDescDTO {
    private String subType;
    private String subtypeDesc;
}
