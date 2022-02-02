package com.sunright.inventory.dto.grn;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class GrnSupplierDTO {
    private String supplierCode;
    private String name;
    private String grnNo;
    private Long grnId;
    protected Set<GrnDetDTO> grnDetails;
}
