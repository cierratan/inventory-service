package com.sunright.inventory.entity.bombypj;

import java.math.BigDecimal;

public interface BombypjDetailProjection {

    String getTranType();
    String getProjectNo();
    String getAlternate();
    String getOrderNo();
    String getAssemblyNo();
    String getGrnNo();
    String getComponent();
    BigDecimal getResvQty();
    BigDecimal getAccumRecdQty();
    BigDecimal getBalQty();
    BigDecimal getRecdQty();
    BigDecimal getShortQty();
    BigDecimal getInTransitQty();
    BigDecimal getDelvQty();
    BigDecimal getPickedQty();
}
