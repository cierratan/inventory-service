package com.sunright.inventory.entity.msr;

import java.math.BigDecimal;

public interface MSRDetailProjection {

    int getSeqNo();
    String getPartNo();
    String getItemNo();
    String getRemarks();
    Integer getItemType();
    String getLoc();
    String getUom();
    String getProjectNo();
    String getGrnNo();
    BigDecimal getRetnQty();
    BigDecimal getRecdQty();
    BigDecimal getRetnPrice();
    long getCountItemNo();
    long getCountPartNo();
    String getMslCode();
}
