package com.sunright.inventory.dto.siv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sunright.inventory.dto.InvBaseDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class SIVDTO extends InvBaseDTO {

    private String subType;

    @NotBlank(message = "Siv No Can Not be Blank !")
    private String sivNo;

    private String projectNo;
    private String currencyCode;
    private BigDecimal currencyRate;
    private String statuz;
    private String entryTime;
    private String tranType;
    private String docmNo;
    private Set<SIVDetailDTO> sivDetails;

    private String entryUser;
    private Date entryDate;

    private String projNoA;
    private String projNoB;
    private String projNoC;
    private String projNoD;
    private String projNoE;

    private String sivNoA;
    private String sivNoB;
    private String sivNoC;
    private String sivNoD;
    private String sivNoE;

    private String sivType;
}
