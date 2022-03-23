package com.sunright.inventory.entity.pur;

import java.math.BigDecimal;
import java.util.Date;

public interface PurDetProjection {
    Integer getSeqNo();
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
    Long getCountItemNo();
    Long getCountPartNo();
    String getDescription();
    String getMslCode();
    BigDecimal getRlseQty();
}
