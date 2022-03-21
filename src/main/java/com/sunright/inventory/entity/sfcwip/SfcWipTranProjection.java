package com.sunright.inventory.entity.sfcwip;

public interface SfcWipTranProjection {
    Integer getCnt();
    Integer getRowSeq();
    String getProductId();
    String getProjectNoSub();
    String getPcbPartNo();
    Integer getSeqNo();
    String getStatus();
}
