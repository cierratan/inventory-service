package com.sunright.inventory.dto.wip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.entity.wip.WipProjDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class WipProjDTO {

    private String companyCode;
    private Integer plantNo;
    private String projectNoSub;
    private String projectNo;
    private String orderNo;
    private String type;
    private String subType;
    private Set<WipProjDetail> wipProjDetails;
}
