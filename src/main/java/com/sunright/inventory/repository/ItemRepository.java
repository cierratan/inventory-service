package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Item;
import com.sunright.inventory.entity.ItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Modifying
    @Query("UPDATE ITEM i set i.alternate = :itemNo " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :obsoleteItem ")
    void updateAlternate(String itemNo, String companyCode, Integer plantNo, String obsoleteItem);

    @Modifying
    @Query("UPDATE ITEM i set i.alternate = null " +
            "WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :qryObsItem ")
    void updateAlternate(String companyCode, Integer plantNo, String qryObsItem);

    List<Item> findByCompanyCodeAndPlantNoAndItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT count(i) as countItemNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo LIKE %:itemNo% " +
            "AND source IN ('B','C')")
    ItemProjection getCountByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT count(i) as countPartNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.partNo LIKE %:partNo% " +
            "AND source IN ('B','C')")
    ItemProjection getCountByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query("SELECT i.partNo as partNo, i.itemNo as itemNo FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo " +
            "AND (partNo LIKE %:partNo% or itemNo LIKE %:itemNo%) AND source IN ('B','C')")
    List<ItemProjection> lovItemPart(String companyCode, Integer plantNo, String partNo, String itemNo);

    @Query("SELECT i.itemNo as itemNo, i.partNo as partNo FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.itemNo LIKE %:itemNo% AND i.source IN ('B','C')")
    List<ItemProjection> getItemAndPartNoByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.itemNo as itemNo, i.partNo as partNo FROM ITEM i WHERE i.companyCode = :companyCode " +
            "AND i.plantNo = :plantNo AND i.partNo LIKE %:partNo% AND i.source IN ('B','C')")
    List<ItemProjection> getItemAndPartNoByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query("SELECT i.partNo as partNo, i.itemNo as itemNo, substring(i.description, 1, 60) as description, " +
            "i.loc as loc, i.uom as uom FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = :itemNo")
    ItemProjection itemInfo(String companyCode, Integer plantNo, String itemNo);

    @Query("SELECT i.source as source FROM ITEM i WHERE i.companyCode = :companyCode AND i.plantNo = :plantNo " +
            "AND i.itemNo = :itemNo")
    ItemProjection getSource(String companyCode, Integer plantNo, String itemNo);
}
