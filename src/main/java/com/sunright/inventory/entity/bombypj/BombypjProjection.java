package com.sunright.inventory.entity.bombypj;

import java.math.BigDecimal;

public interface BombypjProjection {
    String getProjectNo();
    String getAlternate();
    String getOrderNo();
    String getAssemblyNo();
    String getComponent();
    BigDecimal getResvQty();
    BigDecimal getShortQty();
    BigDecimal getPickedQty();
    BigDecimal getMrvResv();

}
