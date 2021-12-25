package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.entity.Status;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class BaseDTO {
    private Long version;
    private Status status;
}
