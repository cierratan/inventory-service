package com.sunright.inventory.entity.itembatclog;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ItemBatcLogProjection {
    String getPoNo();
    BigDecimal getRecdPrice();
    Long getSivQty();
    Long getBatchNo();
    String getSivNo();
    String getItemNo();
    ZonedDateTime getItembatcLogCreatedAt();
    ZonedDateTime getGrnCreatedAt();
    ZonedDateTime getSivCreatedAt();
}
