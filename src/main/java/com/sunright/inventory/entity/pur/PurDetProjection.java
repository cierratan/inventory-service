package com.sunright.inventory.entity.pur;

import java.math.BigDecimal;
import java.util.Date;

public interface PurDetProjection {
    int getSeqNo();
    String getPartNo();
    String getItemNo();
    Integer getRecSeq();
    String getRemarks();
    Integer getItemType();
    String getLoc();
    String getUom();
    String getProjectNo();
    BigDecimal getOrderQty();
    BigDecimal getRecdQty();
    BigDecimal getUnitPrice();
    Date getDueDate();
    BigDecimal getResvQty();
    String getInvUom();
    BigDecimal getStdPackQty();
    String getPoNo();
    long getCountItemNo();
    long getCountPartNo();
    String getDescription();
    String getMslCode();
    BigDecimal getRlseQty();
}
