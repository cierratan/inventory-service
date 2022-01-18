package com.sunright.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.entity.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class InvBaseDTO {
    private Long id;
    private String companyCode;
    private Integer plantNo;

    private Long version;
    private Status status;

    private String createdBy;
    private ZonedDateTime createdAt;
    private String updatedBy;
    private ZonedDateTime updatedAt;
}
