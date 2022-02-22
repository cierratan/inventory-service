package com.sunright.inventory.entity.pr;

import io.swagger.models.auth.In;

import java.math.BigDecimal;

public interface PRDetailProjection {
    Integer getItemType();
    String getAlternate();
    BigDecimal getShortQty();
    BigDecimal getPickedQty();
    String getSaleType();
    String getDocmNo();
    String getPartNo();
    String getUom();
    String getLoc();
    BigDecimal getStdMaterial();
    String getRemarks();
    Long getIssReq();
}
