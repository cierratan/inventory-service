package com.sunright.inventory.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDTO extends BaseDTO {
    private String itemNo;
    private String loc;
}
