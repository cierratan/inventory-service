package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itembatclog.ItemBatcLog;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogId;
import com.sunright.inventory.entity.itembatclog.ItemBatcLogProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemBatcLogRepository extends JpaRepository<ItemBatcLog, ItemBatcLogId> {

    @Query("SELECT DISTINCT il.poNo as poNo, gd.recdPrice as recdPrice FROM ITEMBATC_LOG il JOIN GRNDET gd on gd.seqNo = il.grnSeq " +
            "AND gd.grnNo = il.grnNo WHERE gd.companyCode = :companyCode AND gd.plantNo = :plantNo " +
            "AND il.id.companyCode = :companyCode AND il.id.plantNo = :plantNo AND il.id.sivNo = :sivNo " +
            "AND il.id.batchNo = :batchNo AND il.id.itemNo = :itemNo")
    ItemBatcLogProjection getPoNoRecdPrice(String companyCode, Integer plantNo, String sivNo, Long batchNo, String itemNo);
}
