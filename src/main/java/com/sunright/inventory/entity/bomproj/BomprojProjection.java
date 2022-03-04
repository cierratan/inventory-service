package com.sunright.inventory.entity.bomproj;

import java.math.BigDecimal;
import java.util.Date;

public interface BomprojProjection {

    String getProjectNo();
    String getOrderNo();
    BigDecimal getOrderQty();
    String getOpenClose();
    String getPickedStatus();
    Date getMbiDate();
    String getSivNo();
    Date getExplodeDate();
    Date getResvDate();
    String getLoc();
}
