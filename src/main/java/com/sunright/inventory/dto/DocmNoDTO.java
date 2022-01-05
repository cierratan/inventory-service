package com.sunright.inventory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocmNoDTO extends BaseDTO {

    private String type;
    private String subType;
    private String description;
    private Integer lastGeneratedNo;
    private String prefix;
    private String postfix;
}
