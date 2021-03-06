package com.sunright.inventory.entity.nlctl;

import java.math.BigDecimal;

public interface NLCTLProjection {

    BigDecimal getBatchNo();
    String getInventoryEnabled();
    Integer getInventoryMonth();
    Long getInventoryYear();
}
