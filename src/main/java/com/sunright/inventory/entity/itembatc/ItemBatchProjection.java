package com.sunright.inventory.entity.itembatc;

import java.math.BigDecimal;

public interface ItemBatchProjection {

    Long getBatchNo();
    String getLoc();
    BigDecimal getQoh();
    String getBatchDesc();
    String getBatchNoLoc();
    Integer getDateCode();
    String getPoNo();
    Integer getPoRecSeq();
    String getGrnNo();
    Integer getGrnSeq();
    BigDecimal getOriQoh();
}
