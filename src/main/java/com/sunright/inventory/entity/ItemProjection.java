package com.sunright.inventory.entity;

import java.math.BigDecimal;

public interface ItemProjection {

    Long getCountItemNo();
    Long getCountPartNo();
    String getItemNo();
    String getPartNo();
    String getSource();
    String getDescription();
    String getLoc();
    String getUom();
    BigDecimal getPickedQty();
    BigDecimal getMrvResv();
    BigDecimal getProdnResv();
    BigDecimal getStdMaterial();
    BigDecimal getCostVariance();
    BigDecimal getBatchNo();
    BigDecimal getQoh();
    BigDecimal getOrderQty();
    BigDecimal getYtdProd();
    BigDecimal getYtdIssue();
    String getCategoryCode();
}
