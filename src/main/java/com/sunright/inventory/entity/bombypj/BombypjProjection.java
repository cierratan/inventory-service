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
    BigDecimal getDelvQty();
    String getPartNo();
    String getSource();
    String getDescription();
    String getLoc();
    String getUom();
    BigDecimal getStdMaterial();
    BigDecimal getInTransitQty();
    BigDecimal getIssuedQty();
    String getTranType();
    BigDecimal getRecdQty();
    BigDecimal getMrvQty();
    BigDecimal getMrvResv();
    Long getCountAlternate();
    BigDecimal getTtlSivQty();
    BigDecimal getPaQty();
    BigDecimal getPbQty();
    BigDecimal getPcQty();
    BigDecimal getPdQty();
    BigDecimal getPeQty();
    Integer getCntA();
    Integer getCntB();
    Integer getCntC();
    Integer getCntD();
    Integer getCntE();
    String getProjNoA();
    String getProjNoB();
    String getProjNoC();
    String getProjNoD();
    String getProjNoE();
    BigDecimal getGreatestQty();
}
