package com.sunright.inventory.entity;

public interface ItemProjection {

    long getCountItemNo();
    long getCountPartNo();
    String getItemNo();
    String getPartNo();
    String getSource();
    String getDescription();
    String getLoc();
    String getUom();
}
