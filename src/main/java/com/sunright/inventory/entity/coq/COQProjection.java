package com.sunright.inventory.entity.coq;

import java.math.BigDecimal;

public interface COQProjection {
    String getDocmNo();
    String getDocmType();
    Integer getRecSeq();
    BigDecimal getDocmQty();
    Integer getSeqNo();
}
