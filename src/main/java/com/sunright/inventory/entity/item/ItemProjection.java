package com.sunright.inventory.entity.item;

import java.math.BigDecimal;

public interface ItemProjection {

    Long getCountItemNo();
    Long getCountPartNo();
    Long getItemId();
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
    BigDecimal getRpcResv();
    BigDecimal getYtdReceipt();
}
