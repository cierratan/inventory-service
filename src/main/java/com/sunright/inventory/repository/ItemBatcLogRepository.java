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

    @Query("SELECT sum(sivQty) as sivQty from ITEMBATC_LOG " +
            "where id.companyCode = :companyCode and id.plantNo = :plantNo " +
            "and id.itemNo = :itemNo and id.batchNo = :batchNo and id.sivNo = :sivNo ")
    ItemBatcLogProjection getSivQty(String companyCode, Integer plantNo, String itemNo, Long batchNo, String sivNo);

    @Query("SELECT l.id.sivNo, l.id.batchNo, l.sivQty, l.id.itemNo, " +
            "l.createdAt as itembatcLogCreatedAt, g.createdAt as grnCreatedAt, s.createdAt as sivCreatedAt " +
            "FROM ITEMBATC_LOG l " +
            "   left join GRN g on g.companyCode = l.id.companyCode and g.plantNo = l.id.plantNo and g.grnNo = l.grnNo " +
            "   inner join SIV s on s.companyCode = l.id.companyCode and s.plantNo = l.id.plantNo and s.sivNo = l.id.sivNo " +
            "WHERE l.id.companyCode = :companyCode and l.id.plantNo = :plantNo " +
            "and l.id.itemNo = :itemNo and l.id.batchNo = :batchNo and l.id.sivNo = :sivNo " +
            "order by g.createdAt desc ")
    ItemBatcLogProjection getBatchLog(String companyCode, Integer plantNo, String itemNo, Long batchNo, String sivNo);
}
