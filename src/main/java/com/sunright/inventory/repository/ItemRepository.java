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

    @Query("SELECT i.itemNo as itemNo, i.partNo as partNo FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.partNo LIKE %:partNo% AND i.source IN ('B','C')")
    List<ItemProjection> getItemAndPartNoByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query("SELECT i.partNo as partNo, i.itemNo as itemNo, substring(i.description, 1, 60) as description, " +
            "i.loc as loc, i.uom as uom, i.pickedQty as pickedQty, i.mrvResv as mrvResv, i.prodnResv as prodnResv " +
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

    @Query("select b.id.projectNo as projectNo, b.id.alternate as alternate , i.partNo as partNo, i.loc as loc, i.uom as uom, l.stdMaterial as stdMaterial, " +
            "coalesce(sum(coalesce(b.shortQty, 0)),0, sum(coalesce(b.resvQty, 0) - coalesce(b.inTransitQty, 0) - coalesce(b.pickedQty, 0)), " +
            "sum(coalesce(b.shortQty, 0))) as shortQty, sum(coalesce(b.pickedQty, 0)) as pickedQty " +
            "from BOMBYPJ b join ITEM i on i.companyCode = b.id.companyCode and i.plantNo = b.id.plantNo and i.itemNo = b.id.alternate join ITEMLOC l " +
            "on l.loc = i.loc and l.companyCode = i.companyCode and l.plantNo = i.plantNo and l.itemNo = i.itemNo " +
            "where b.id.companyCode = :companyCode and b.id.plantNo = :plantNo and coalesce(b.statuz, 'R') NOT IN ('D', 'X') " +
            "and (coalesce(b.pickedQty, 0) > 0 or (coalesce(coalesce(b.shortQty, 0),0, " +
            "(coalesce(b.resvQty, 0) - coalesce(b.inTransitQty, 0) - coalesce(b.pickedQty, 0)), coalesce(b.shortQty, 0)) > 0)) " +
            "and b.shortQty = :projectNo group by b.id.projectNo, b.id.alternate,i.partNo,i.loc,i.uom,l.stdMaterial ORDER BY b.id.alternate")
    List<ItemProjection> getDataByProjectNo(String companyCode, Integer plantNo, String projectNo);
}
