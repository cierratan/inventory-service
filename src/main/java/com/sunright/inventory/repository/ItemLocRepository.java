package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itemloc.ItemLoc;
import com.sunright.inventory.entity.itemloc.ItemLocProjection;
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
    @Query("UPDATE ITEMLOC i set i.mrvResv = :mrvResv " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updateMrvResv(BigDecimal mrvResv, String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh, i.ytdProd = :ytdProd, i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate  " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo and i.loc = :loc ")
    void updateQohYtdProdYtdIssueLastTranDate(BigDecimal qoh, BigDecimal ytdProd, BigDecimal ytdIssue,
                                              Date lastTranDate, String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select (coalesce (i.qoh, 0) - coalesce(i.pickedQty, 0) - coalesce(i.rpcResv, 0) - coalesce(i.mrvResv, 0)) as availQty " +
            "from ITEMLOC i where i.companyCode = :companyCode and i.plantNo = :plantNo " +
            "and i.itemNo = :itemNo and i.loc = (select c.stockLoc from COMPANY c where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo)")
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

    @Query("select coalesce(l.qoh,0) as qoh, coalesce(l.orderQty,0) as orderQty, coalesce(l.prodnResv,0) as prodnResv, " +
            "l.stdMaterial as stdMaterial from ITEMLOC l where l.companyCode = :companyCode and l.plantNo = :plantNo " +
            "and l.itemNo = :itemNo and l.loc = :loc")
    List<ItemLocProjection> qohCur(String companyCode, Integer plantNo, String itemNo, String loc);

    @Query("select count(l) as recCnt, l.loc as loc from ITEMLOC l where l.loc = :loc " +
            "and l.companyCode = :companyCode and l.plantNo = :plantNo and l.itemNo = :itemNo group by l.loc")
    ItemLocProjection getItemLocByItemNo(String companyCode, Integer plantNo, String loc, String itemNo);

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
    @Query("UPDATE ITEMLOC i set i.batchNo = :batchNo " +
            "WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateBatchNo(BigDecimal batchNo, String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.costVariance = :newCostVar, i.stdMaterial = :newStdMat, " +
            "i.ytdReceipt = :ytdReceipt, i.batchNo = :newBatchNo, i.lastTranDate = :lastTranDate " +
            "WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateStdMatYtdRecBatchNoLTranDate(BigDecimal newCostVar, BigDecimal newStdMat,
                                            BigDecimal ytdReceipt, BigDecimal newBatchNo,
                                            Date lastTranDate, String companyCode,
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
    @Query("UPDATE ITEMLOC i set i.stdMaterial = :newStdMat, i.ytdReceipt = :ytdReceipt, " +
            "i.batchNo = :newBatchNo, i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc <> :loc")
    void updateStdMatYtdRecBatchNoLTranDate(BigDecimal newStdMat, BigDecimal ytdReceipt, BigDecimal newBatchNo,
                                            Date lastTranDate, String companyCode,
                                            Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.stdMaterial = :newStdMat, i.costVariance = :costVariance, i.ytdReceipt = :ytdReceipt, " +
            "i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateStdMatCostVarianceYtdRecLTranDate(BigDecimal newStdMat, BigDecimal costVariance, BigDecimal ytdReceipt,
                                            Date lastTranDate, String companyCode,
                                            Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.stdMaterial = :newStdMat, i.ytdReceipt = :ytdReceipt, " +
            "i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc <> :loc")
    void updateStdMatYtdRecLTranDateWithNotEqualLoc(BigDecimal newStdMat, BigDecimal ytdReceipt,
                                                 Date lastTranDate, String companyCode,
                                                 Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    void updateQoh(BigDecimal qoh, String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.qoh = :qoh WHERE i.id = :id")
    void updateQoh(BigDecimal qoh, Long id);

    @Modifying
    @Query("UPDATE ITEMLOC i set i.pickedQty = :pickedQtyUpdate, " +
            "i.prodnResv = :prodnResvUpdate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updatePickedQtyProdnResv(BigDecimal pickedQtyUpdate, BigDecimal prodnResvUpdate, String companyCode,
                                  Integer plantNo, String itemNo, String loc);

    @Query("select ib.id as id, ib.qoh as qoh, ib.stdMaterial as stdMaterial, coalesce(ib.batchNo,0) as batchNo from ITEMLOC ib " +
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

    @Query("select ib.itemNo as itemNo, ib.stdMaterial as stdMaterial from ITEMLOC ib " +
            "where ib.companyCode = :companyCode and ib.plantNo = :plantNo and ib.itemNo = :itemNo and ib.loc = :loc")
    ItemLocProjection itemLocCur(String companyCode, Integer plantNo, String itemNo, String loc);

    @Query(value = "SELECT nvl(s1.prodn_resv, 0) AS prodnResv, nvl(s2.resv_qty, 0) AS resvQty, nvl(s3.po_resv_qty, 0) AS poResvQty " +
            "FROM (SELECT SUM(nvl(i.prodn_resv, 0)) prodn_resv FROM itemloc i WHERE i.company_code = :companyCode AND i.plant_no = :plantNo " +
            "AND i.item_no = :itemNo) s1, (SELECT SUM(nvl(b.resv_qty, 0)) resv_qty FROM bombypj b " +
            "WHERE b.company_code = :companyCode AND b.plant_no = :plantNo AND nvl(b.resv_qty, 0) <> 0 " +
            "AND b.alternate = :itemNo) s2, (SELECT SUM(nvl(pd.resv_qty, 0)) po_resv_qty " +
            "FROM purdet pd, pur p WHERE  p.company_code = pd.company_code AND p.plant_no = pd.plant_no " +
            "AND nvl(p.open_close, 'O') NOT IN ( 'V' ) AND p.po_no = pd.po_no AND pd.company_code = :companyCode " +
            "AND pd.plant_no = :plantNo AND nvl(pd.adv_status, 'N') = 'Y' " +
            "AND nvl(pd.order_qty, 0) > nvl(pd.recd_qty, 0) AND pd.item_no = :itemNo) s3", nativeQuery = true)
    ItemLocProjection getResv(String companyCode, Integer plantNo, String itemNo);

    ItemLoc findItemLocByCompanyCodeAndPlantNoAndItemId(String companyCode, Integer plantNo, Long id);

    @Query(value = "SELECT il.recCnt as recCnt, l.ID as id, l.LOC as loc " +
            "FROM (select count(*) as recCnt, i.COMPANY_CODE, i.PLANT_NO, i.ITEM_NO, i.LOC from ITEMLOC i ) il left join" +
            "   ITEMLOC l on l.COMPANY_CODE = il.COMPANY_CODE and l.PLANT_NO = il.PLANT_NO and l.ITEM_NO = il.PLANT_NO and l.LOC = il.LOC " +
            "WHERE il.COMPANY_CODE = :companyCode and il.PLANT_NO = :plantNo and il.ITEM_NO = :itemNo and il.LOC = :loc", nativeQuery = true)
    ItemLocProjection findItemLocWithRecCnt(String companyCode, Integer plantNo, String itemNo, String loc);
}