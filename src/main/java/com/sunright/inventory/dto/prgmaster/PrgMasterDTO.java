package com.sunright.inventory.dto.prgmaster;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class PrgMasterDTO {

    private String prgId;
    private String prgDesc;
    private String moduleCd;
    private String type;
    private String filePath;
    private String remarks;
    private String heading;
    private Integer seq;
    private String specialType;
    private String display;
}
