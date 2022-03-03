package com.sunright.inventory.entity.msr;

import java.math.BigDecimal;

public interface MSRDetailProjection {

    String getMsrNo();
    Integer getSeqNo();
    String getPartNo();
    String getItemNo();
    String getRemarks();
    Integer getItemType();
    String getLoc();
    String getUom();
    String getProjectNo();
    String getPoNo();
    String getGrnNo();
    String getGrnType();
    Integer getGrndetSeqNo();
    BigDecimal getRetnQty();
    BigDecimal getRecdQty();
    BigDecimal getRecdPrice();
    BigDecimal getGrndetRecdQty();
    BigDecimal getRetnPrice();
    String getMslCode();
    Long getBatchNo();
    Long getCountItemNo();
    Long getCountPartNo();
}
