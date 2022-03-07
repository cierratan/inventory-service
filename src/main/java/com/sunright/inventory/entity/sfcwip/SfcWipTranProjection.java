package com.sunright.inventory.entity.sfcwip;

public interface SfcWipTranProjection {
    Integer getCnt();
    String getProductId();
    String getProjectNoSub();
    String getPcbPartNo();
    Integer getSeqNo();
    String getStatus();
}
