package com.sunright.inventory.entity.grn;

import java.math.BigDecimal;

public interface GrnDetailProjection {

    String getGrnNo();
    Integer getSeqNo();
    String getUom();
    BigDecimal getIssuedQty();
}
