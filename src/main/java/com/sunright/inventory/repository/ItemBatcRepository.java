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

    @Query(value = "select 1 num, batch_no as batchNo, loc as loc, qoh as qoh " +
            "from itembatc  " +
            "where company_code = :companyCode" +
            "  and plant_no = :plantNo" +
            "  and item_no = :alternate" +
            "  and loc = (select stock_loc" +
            "             from company" +
            "             where company_code = :companyCode" +
            "               and plant_no = :plantNo)" +
            "  and nvl(qoh, 0) > 0 " +
            "union " +
            "select 2 num, batch_no as batchNo, loc as loc, qoh as qoh " +
            "from itembatc  " +
            "where company_code = :companyCode" +
            "  and plant_no = :plantNo" +
            "  and item_no = :alternate" +
            "  and loc <> (select stock_loc" +
            "  from company" +
            "   where company_code = :companyCode    " +
            " and plant_no = :plantNo)" +
            "       and nvl(qoh,0) > 0 order by 1,2", nativeQuery = true)
    List<ItemBatchProjection> getBatchNoByItemNo(String companyCode, Integer plantNo, String alternate);

    @Query("select distinct concat(ib.id.batchNo,'/',ib.id.loc,'/',ib.qoh) as batchDesc, " +
            "concat(ib.id.batchNo,'/',ib.id.loc) as batchNoLoc from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo")
    List<ItemBatchProjection> getItemBatchByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("select distinct concat(ib.id.batchNo,'/',ib.id.loc,'/',ib.qoh) as batchDesc, " +
            "concat(ib.id.batchNo,'/',ib.id.loc) as batchNoLoc from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo and ib.id.loc <> (select c.stockLoc from COMPANY c " +
            "where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
    List<ItemBatchProjection> getItemBatchFLocByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("select ib.qoh as qoh, ib.dateCode as dateCode, ib.poNo as poNo, ib.poRecSeq as poRecSeq, ib.grnNo as grnNo, " +
            "ib.grnSeq as grnSeq, ib.oriQoh as oriQoh from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo and ib.id.batchNo = :batchNo and ib.id.loc = :loc")
    List<ItemBatchProjection> getItemBatchByBatchNo(String companyCode, Integer plantNo, String itemNo, Long batchNo, String loc);

    @Modifying
    @Query("DELETE FROM ITEMBATC i WHERE i.id.itemNo = :itemNo AND i.id.batchNo = :batchNo")
    void deleteItemBatcBal(String itemNo, Long batchNo);

    @Modifying
    @Query("UPDATE ITEMBATC i SET i.qoh = :itemBatcBal WHERE i.id.itemNo = :itemNo and i.id.batchNo = :batchNo")
    void updateItemBatcBal(BigDecimal itemBatcBal, Long batchNo);

    @Query("select count(ib) as countItemBatc from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo")
    ItemBatchProjection itembatcCur(String companyCode, Integer plantNo, String itemNo);

    @Query("select ib.id.batchNo as batchNo, ib.qoh as qoh from ITEMBATC ib where ib.id.companyCode = :companyCode " +
            "and ib.id.plantNo = :plantNo and ib.id.itemNo = :itemNo and ib.qoh >= :ttlSivQty")
    List<ItemBatchProjection> cBatch(String companyCode, Integer plantNo, String itemNo, BigDecimal ttlSivQty);

    @Query("select max(i.id.batchNo) as maxBatchNo from ITEMBATC i " +
            "where i.id.companyCode = :companyCode and i.id.plantNo = :plantNo and i.id.itemNo = :itemNo")
    ItemBatchProjection getMaxBatchNo(String companyCode, Integer plantNo, String itemNo);
}
