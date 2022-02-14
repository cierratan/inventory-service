package com.sunright.inventory.entity.sfcwip;

import java.math.BigDecimal;

public interface SfcWipProjection {
    String getProjectNoSub();
    String getPcbPartNo();
    BigDecimal getPcbQty();
}
