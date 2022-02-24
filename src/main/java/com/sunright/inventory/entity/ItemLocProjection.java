package com.sunright.inventory.entity;

import java.math.BigDecimal;

public interface ItemLocProjection {

    Long getId();
    BigDecimal getAvailQty();
    BigDecimal getQoh();
    BigDecimal getStdMaterial();
    BigDecimal getEoh();
    Long getRecCnt();
    String getLoc();
    BigDecimal getBatchNo();
    BigDecimal getCostVariance();
    BigDecimal getOrderQty();
    BigDecimal getPickedQty();
    BigDecimal getYtdReceipt();
    BigDecimal getProdnResv();
    BigDecimal getYtdProd();
    BigDecimal getYtdIssue();
    String getItemNo();
    BigDecimal getPoResvQty();
    BigDecimal getResvQty();
}
