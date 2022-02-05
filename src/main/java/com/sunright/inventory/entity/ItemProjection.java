package com.sunright.inventory.entity;

import java.math.BigDecimal;

public interface ItemProjection {

    long getCountItemNo();
    long getCountPartNo();
    String getItemNo();
    String getPartNo();
    String getSource();
    String getDescription();
    String getLoc();
    String getUom();
    BigDecimal getPickedQty();
    BigDecimal getMrvResv();
    BigDecimal getProdnResv();

}
