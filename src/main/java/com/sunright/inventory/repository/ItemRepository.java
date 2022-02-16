package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.ItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Modifying
    @Query("UPDATE ITEM i set i.alternate = :itemNo " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :obsoleteItem ")
    void updateAlternate(String itemNo, String companyCode, Integer plantNo, String obsoleteItem);

    @Modifying
    @Query("UPDATE ITEM i set i.alternate = null WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :qryObsItem ")
    void updateAlternate(String companyCode, Integer plantNo, String qryObsItem);

    @Modifying
    @Query("UPDATE ITEM i set i.pickedQty = :pickedQty, i.mrvResv = :mrvResv, i.prodnResv = :prodnResv " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo ")
    void updatePickedQtyMrvResvProdnResv(BigDecimal pickedQty, BigDecimal mrvResv, BigDecimal prodnResv, String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEM i set i.qoh = :qoh, i.ytdProd = :ytdProd, i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo ")
    void updateQohYtdProdYtdIssueLastTranDate(BigDecimal qoh, BigDecimal ytdProd, BigDecimal ytdIssue, Date lastTranDate, String companyCode, Integer plantNo, String itemNo);

    List<Item> findByCompanyCodeAndPlantNoAndItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT count(i) as countItemNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo LIKE %:itemNo% " +
            "AND i.source IN ('B','C')")
    ItemProjection getCountByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT count(i) as countPartNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.partNo LIKE %:partNo% " +
            "AND i.source IN ('B','C')")
    ItemProjection getCountByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query("SELECT i.partNo as partNo, i.itemNo as itemNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo " +
            "AND (partNo LIKE %:partNo% or itemNo LIKE %:itemNo%) AND i.source IN ('B','C')")
    List<ItemProjection> lovItemPart(String companyCode, Integer plantNo, String partNo, String itemNo);

    @Query("SELECT i.itemNo as itemNo, i.partNo as partNo FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo LIKE %:itemNo% AND i.source IN ('B','C')")
    List<ItemProjection> getItemAndPartNoByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.itemNo as itemNo, i.loc as loc, i.uom as uom, i.partNo as partNo, i.description as description, " +
            "COALESCE(i.stdMaterial,0) as stdMaterial FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND (i.partNo LIKE %:partNo% OR i.itemNo LIKE %:itemNo%) AND i.source IN ('B','C')")
    List<ItemProjection> getItemAndPartNoByPartNo(String companyCode, Integer plantNo, String partNo, String itemNo);

    @Query("SELECT i.partNo as partNo, i.itemNo as itemNo, i.description as description, " +
            "i.loc as loc, i.uom as uom, COALESCE(i.pickedQty,0) as pickedQty, i.mrvResv as mrvResv, COALESCE(i.prodnResv,0) as prodnResv, " +
            "COALESCE(i.qoh,0) as qoh, COALESCE(i.ytdProd,0) as ytdProd, COALESCE(i.ytdIssue,0) as ytdIssue, " +
            "COALESCE(i.stdMaterial,0) as stdMaterial " +
            "FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    ItemProjection itemInfo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.source as source FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo " +
            "AND i.itemNo = :itemNo")
    ItemProjection getSource(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.partNo as partNo, i.loc as loc, i.uom as uom ,i.source as source, l.stdMaterial as stdMaterial, " +
            "SUM(COALESCE(b.pickedQty, 0)) as pickedQty FROM BOMBYPJ b join ITEM i join ITEMLOC l on l.companyCode = i.companyCode " +
            "AND l.plantNo = i.plantNo AND l.itemNo = i.itemNo AND l.loc = i.loc AND i.companyCode = b.id.companyCode " +
            "AND i.plantNo = b.id.plantNo AND i.itemNo = b.id.alternate WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND COALESCE(b.statuz, 'R') NOT IN ('D', 'X') AND b.id.projectNo = :projectNo " +
            "AND b.id.alternate = :itemNo GROUP BY i.partNo, i.loc, i.uom, i.source, l.stdMaterial")
    ItemProjection getDataByProjectNoAndItemNo(String companyCode, Integer plantNo, String projectNo, String itemNo);

    @Query("SELECT i.itemNo as itemNo FROM ITEM i WHERE i.itemNo = :projectNo")
    ItemProjection getItemNoByProjectNo(String projectNo);

    @Query("SELECT i.itemNo as itemNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo")
    List<ItemProjection> getItemNoByCompanyCodeAndPlantNo(String companyCode, Integer plantNo);

    @Query("SELECT coalesce(i.qoh,0) as qoh, coalesce(i.orderQty,0) as orderQty, coalesce(i.costVariance,0) as costVariance, " +
            "coalesce(i.stdMaterial,0) as stdMaterial, i.batchNo as batchNo FROM ITEM i " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    ItemProjection getDataItemCur(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.uom as uom FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    ItemProjection getItemUomByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEM i set i.qoh = :qoh, i.orderQty = :itemOrderQty, i.stdMaterial = :newStdMat, i.costVariance = :newCostVar, " +
            "i.ytdReceipt = :ytdReceipt, i.lastTranDate = :lastTranDate, i.lastPurPrice = :convCost, i.batchNo = :newBatchNo " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    void updateDataItems(BigDecimal qoh, BigDecimal itemOrderQty, BigDecimal newStdMat, BigDecimal newCostVar,
                         BigDecimal ytdReceipt, Date lastTranDate, BigDecimal convCost, BigDecimal newBatchNo,
                         String companyCode, Integer plantNo, String itemNo);

    @Modifying
    @Query("UPDATE ITEM i set i.pickedQty = :pickedQty, i.prodnResv = :prodnResv WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    void updatePickedQtyProdnResv(BigDecimal pickedQty, BigDecimal prodnResv, String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.qoh as qoh FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo " +
            "AND i.itemNo = :itemNo AND i.loc = :loc")
    ItemProjection getQohByItemNo(String companyCode, Integer plantNo, String itemNo, String loc);

    @Modifying
    @Query("UPDATE ITEM i set i.prodnResv = :prodnResv, i.pickedQty = :pickedQty, i.qoh = :qoh, i.ytdProd = :ytdProd, " +
            "i.ytdIssue = :ytdIssue, i.lastTranDate = :lastTranDate WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo AND i.loc = :loc")
    void updateProdnResvPickedQtyQohYtdProdTydIssueLTranDate(BigDecimal prodnResv, BigDecimal pickedQty, BigDecimal qoh,
                                                             BigDecimal ytdProd, BigDecimal ytdIssue, Date lastTranDate, String companyCode,
                                                             Integer plantNo, String itemNo, String loc);

    @Query("SELECT i.categoryCode as categoryCode, i.partNo as partNo FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    ItemProjection itemCatCodePartNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT s.itemNo as itemNo, i.loc as loc, i.uom as uom FROM ITEM i join SALEDET s on s.id.companyCode = i.companyCode " +
            "AND s.id.plantNo = i.plantNo AND s.itemNo = i.itemNo WHERE s.id.orderNo =:docmNo AND i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    List<ItemProjection> itemCur(String docmNo, String companyCode, Integer plantNo, String itemNo);
}