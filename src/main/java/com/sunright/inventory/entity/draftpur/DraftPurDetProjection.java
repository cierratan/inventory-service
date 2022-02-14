package com.sunright.inventory.entity.draftpur;

import java.math.BigDecimal;

public interface DraftPurDetProjection {
    String getPoNo();
    String getOpenClose();
    BigDecimal getResvQty();
    BigDecimal getRlseQty();
    BigDecimal getRecdQty();
}
