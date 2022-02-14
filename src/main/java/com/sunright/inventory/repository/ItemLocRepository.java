package com.sunright.inventory.repository;

import com.sunright.inventory.entity.ItemLoc;
import com.sunright.inventory.entity.ItemLocProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface ItemLocRepository extends JpaRepository<ItemLoc, Long> {
    List<ItemLoc> findByCompanyCodeAndPlantNoAndItemNoAndLoc(String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.pickedQty = :pickedQty, i.mrvResv = :mrvResv, i.prodnResv = :prodnResv " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updatePickedQtyMrvResvProdnResv(BigDecimal pickedQty, BigDecimal mrvResv, BigDecimal prodnResv, String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh, i.ytdProd = :ytdProd, i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate  " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updateQohYtdProdYtdIssueLastTranDate(BigDecimal qoh, BigDecimal ytdProd, BigDecimal ytdIssue,
                                              Date lastTranDate, String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select (coalesce (i.qoh, 0) - coalesce(i.pickedQty, 0) - coalesce(i.rpcResv, 0) - coalesce(i.mrvResv, 0)) as availQty " +
            "from ITEMLOC i where i.companyCode = :companyCode and i.plantNo = :plantNo " +
            "and i.itemNo = :itemNo and i.loc = (select c.stockLoc from COMPANY c " +
            "where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
    ItemLocProjection getItemAvailQohL(String companyCode, Integer plantNo, String itemNo);

    @Query("select sum(coalesce(i.qoh,0)) as availQty from ITEMLOC i where i.companyCode = :companyCode " +
            "and i.plantNo = :plantNo and i.itemNo = :itemNo and i.loc <> (select c.stockLoc from COMPANY c " +
            "where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
    ItemLocProjection getItemAvailQohF(String companyCode, Integer plantNo, String itemNo);

    @Query("select distinct coalesce(l.qoh,0) as qoh, l.stdMaterial as stdMaterial, " +
            "(coalesce(l.qoh, 0) - (coalesce(l.prodnResv, 0) - " +
            "coalesce((select sum(d.resvQty - coalesce(d.accumRecdQty,0)) from BOMBYPJ_DET d " +
            "where d.id.projectNo = d.id.assemblyNo and d.resvQty <> coalesce(d.accumRecdQty,0) " +
            "and coalesce(d.status,'O') not in ('C') and d.tranType = 'PRJ'), 0)) - coalesce(l.rpcResv, 0) - coalesce(l.mrvResv, 0)) as eoh " +
            "from ITEMLOC l left join BOMBYPJ_DET b " +
            "on b.id.companyCode = l.companyCode and b.id.plantNo = l.plantNo and b.id.alternate = l.itemNo " +
            "where l.companyCode = :companyCode and l.plantNo = :plantNo and l.itemNo = :itemNo and l.loc = :loc")
    ItemLocProjection getQohCur(String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select count(l) as recCnt, l.loc as loc from ITEMLOC l where l.loc = :loc " +
            "and l.companyCode = :companyCode and l.plantNo = :plantNo and l.itemNo = :itemNo group by l.loc")
    ItemLocProjection getItemLocByItemNo(String companyCode, Integer plantNo, String loc, String itemNo);

    @Query("select l.batchNo as batchNo, sum(coalesce(l.costVariance,0)) as costVariance, " +
            "sum(coalesce(l.qoh,0)) as qoh, sum(coalesce(l.orderQty,0)) as orderQty, coalesce(l.stdMaterial,0) as stdMaterial " +
            "from ITEMLOC l where l.companyCode = :companyCode and l.plantNo = :plantNo " +
            "and l.itemNo = :itemNo group by l.batchNo, l.stdMaterial")
    ItemLocProjection getItemLocSumByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.orderQty = :qoh, i.costVariance = :newCostVar, i.stdMaterial = :newStdMat, " +
            "i.ytdReceipt = :ytdReceipt, i.batchNo = :newBatchNo, i.lastTranDate = :lastTranDate, " +
            "i.lastPurPrice = :convCost WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateQohVarianceStdMatYtdRecBatchNoLTranDateLPurPrice(BigDecimal qoh, BigDecimal newCostVar, BigDecimal newStdMat,
                                                                BigDecimal ytdReceipt, BigDecimal newBatchNo,
                                                                Date lastTranDate, BigDecimal convCost, String companyCode,
                                                                Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.stdMaterial = :newStdMat, i.ytdReceipt = :ytdReceipt, " +
            "i.batchNo = :newBatchNo, i.lastTranDate = :lastTranDate, " +
            "i.lastPurPrice = :convCost WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc <> :loc")
    void updateStdMatYtdRecBatchNoLTranDateLPurPrice(BigDecimal newStdMat, BigDecimal ytdReceipt, BigDecimal newBatchNo,
                                                     Date lastTranDate, BigDecimal convCost, String companyCode,
                                                     Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    void updateQoh(BigDecimal qoh, String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.pickedQty = :pickedQtyUpdate, " +
            "i.prodnResv = :prodnResvUpdate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updatePickedQtyProdnResv(BigDecimal pickedQtyUpdate, BigDecimal prodnResvUpdate, String companyCode,
                                  Integer plantNo, String itemNo, String loc);

    @Query("select ib.qoh as qoh, ib.stdMaterial as stdMaterial, coalesce(ib.batchNo,0) as batchNo from ITEMLOC ib " +
            "where ib.companyCode = :companyCode and ib.plantNo = :plantNo and ib.itemNo = :itemNo and ib.loc = :loc")
    ItemLocProjection itemLocByItemNo(String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select coalesce(ib.ytdReceipt,0) as ytdReceipt, coalesce(ib.qoh,0) as qoh, " +
            "coalesce(ib.pickedQty,0) as pickedQty, coalesce(ib.prodnResv,0) as prodnResv, " +
            "coalesce(ib.ytdIssue,0) as ytdIssue, coalesce(ib.ytdProd,0) as ytdProd from ITEMLOC ib " +
            "where ib.companyCode = :companyCode and ib.plantNo = :plantNo and ib.itemNo = :itemNo and ib.loc = :loc")
    ItemLocProjection itemLocInfo(String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.prodnResv = :prodnResv, i.pickedQty = :pickedQty, i.qoh = :qoh, i.ytdProd = :ytdProd, " +
            "i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(BigDecimal prodnResv, BigDecimal pickedQty, BigDecimal qoh,
                                                             BigDecimal ytdProd, BigDecimal ytdIssue, Date lastTranDate, String companyCode,
                                                             Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.prodnResv = :prodnResv, i.pickedQty = :pickedQty WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateProdnResvPickedQty(BigDecimal prodnResv, BigDecimal pickedQty, String companyCode,
                                  Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh, i.ytdProd = :ytdProd, " +
            "i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateQohYtdProdTydIssueLTranDate(BigDecimal qoh, BigDecimal ytdProd, BigDecimal ytdIssue,
                                           Date lastTranDate, String companyCode,
                                           Integer plantNo, String itemNo, String loc);

    @Query("select ib.itemNo as itemNo from ITEMLOC ib " +
            "where ib.companyCode = :companyCode and ib.plantNo = :plantNo and ib.itemNo = :itemNo and ib.loc = :loc")
    List<ItemLocProjection> itemLocCur(String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select distinct coalesce((select sum(coalesce(i.prodnResv,0)) from ITEMLOC il where il.companyCode = :companyCode " +
            "and il.plantNo = :plantNo and il.itemNo = :itemNo),0) as prodnResv, " +
            "coalesce((select sum(coalesce(b.resvQty,0)) from BOMBYPJ b where b.id.companyCode = :companyCode " +
            "and b.id.plantNo = :plantNo and coalesce(b.resvQty,0) <> 0 and b.id.alternate = :itemNo),0) as resvQty, " +
            "coalesce((select sum(coalesce(pd.resvQty,0)) from PURDET pd join PUR p on p.id.companyCode = pd.id.companyCode " +
            "and p.id.plantNo = pd.id.plantNo and coalesce(p.openClose,'O') not in ('V') and p.id.poNo = pd.id.poNo " +
            "and pd.id.companyCode = :companyCode and pd.id.plantNo = :plantNo and coalesce(pd.advStatus,'N') = 'Y' " +
            "and pd.itemNo = :itemNo),0) as poResvQty from ITEMLOC i where i.companyCode = :companyCode and i.plantNo = :plantNo " +
            "and i.itemNo = :itemNo")
    ItemLocProjection getResv(String companyCode, Integer plantNo, String itemNo);
}