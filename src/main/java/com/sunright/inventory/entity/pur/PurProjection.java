package com.sunright.inventory.entity.pur;

import java.math.BigDecimal;
import java.util.Date;

public interface PurProjection {

    String getPoNo();
    String getSupplierCode();
    String getCurrencyCode();
    BigDecimal getCurrencyRate();
    String getBuyer();
    Date getRlseDate();
    String getRemarks();
    String getOpenClose();
}
