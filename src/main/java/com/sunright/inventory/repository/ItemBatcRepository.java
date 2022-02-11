package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itembatc.ItemBatc;
import com.sunright.inventory.entity.itembatc.ItemBatcId;
import com.sunright.inventory.entity.itembatc.ItemBatchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ItemBatcRepository extends JpaRepository<ItemBatc, ItemBatcId> {

    @Modifying
    @Query("UPDATE ITEMBATC i " +
            "SET i.qoh = :balance " +
            "WHERE i.id.companyCode = :companyCode and i.id.plantNo = :plantNo and i.id.batchNo = :batchNo " +
            " and i.id.itemNo = :itemNo and i.id.loc = :loc")
    void updateQoh(BigDecimal balance, String companyCode, Integer plantNo, Long batchNo, String itemNo, String loc);

    @Query("select ib.id.batchNo as batchNo, ib.id.loc as loc, ib.qoh as qoh " +
            "from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo " +
            "and ib.id.loc = (select c.stockLoc from COMPANY c where c.id.companyCode = :companyCode " +
            "and c.id.plantNo = :plantNo) " +
            "or ib.id.loc <> (select c.stockLoc from COMPANY c where c.id.companyCode = :companyCode " +
            "and c.id.plantNo = :plantNo) and coalesce(qoh, 0) > 0")
    List<ItemBatchProjection> getBatchNoByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("select distinct concat(ib.id.batchNo,'/',ib.id.loc,'/',ib.qoh) as batchDesc, " +
            "concat(ib.id.batchNo,'/',ib.id.loc) as batchNoLoc from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo")
    List<ItemBatchProjection> getItemBatchByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("select distinct concat(ib.id.batchNo,'/',ib.id.loc,'/',ib.qoh) as batchDesc, " +
            "concat(ib.id.batchNo,'/',ib.id.loc) as batchNoLoc from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo and ib.id.loc <> (select c.stockLoc from COMPANY c " +
            "where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
    List<ItemBatchProjection> getItemBatchFLocByItemNo(String companyCode, Integer plantNo, String itemNo);
}
