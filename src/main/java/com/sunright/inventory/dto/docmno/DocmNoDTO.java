package com.sunright.inventory.dto.docmno;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.BaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class DocmNoDTO extends BaseDTO {

    private String type;
    private String subType;
    private String description;
    private Integer lastGeneratedNo;
    private String prefix;
    private String postfix;
}
