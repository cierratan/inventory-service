package com.sunright.inventory.dto.wip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.entity.wip.WipDirsDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class WipDirsDTO {

    private String companyCode;
    private Integer plantNo;
    private String orderNo;
    private String itemNo;
    private Set<WipDirsDetail> wipDirsDetails;
}
