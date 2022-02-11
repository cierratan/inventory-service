package com.sunright.inventory.entity;

import java.math.BigDecimal;

public interface ItemLocProjection {

    BigDecimal getAvailQty();
    BigDecimal getQoh();
    BigDecimal getStdMaterial();
    BigDecimal getEoh();
    Long getRecCnt();
    String getLoc();
    BigDecimal getBatchNo();
    BigDecimal getCostVariance();
    BigDecimal getOrderQty();
}
