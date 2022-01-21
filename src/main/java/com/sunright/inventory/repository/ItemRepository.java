package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Item;
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
            "WHERE i.companyCode = :companyCode and i.plantNo = :plantNo and i.itemNo = :obsoleteItem ")
    void updateAlternate(String itemNo, String companyCode, Integer plantNo, String obsoleteItem);

    @Modifying
    @Query("UPDATE ITEM i set i.alternate = null " +
            "WHERE i.companyCode = :companyCode and i.plantNo = :plantNo and i.itemNo = :qryObsItem ")
    void updateAlternate(String companyCode, Integer plantNo, String qryObsItem);

    List<Item> findByCompanyCodeAndPlantNoAndItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query(value = "select count(i) from ITEM i where company_code = :companyCode and plant_no = :plantNo and item_no like %:itemNo% " +
            "and source in ('B','C')")
    Long countByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query(value = "select count(i) from ITEM where company_code = :companyCode and plant_no = :plantNo and part_no like %:partNo% " +
            "and source in ('B','C')")
    Long countByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query(value = "select partNo, itemNo from ITEM where company_code = :companyCode and plant_no = :plantNo " +
            "and (part_no like %:partNo% or item_no like %:itemNo%) and source in ('B','C')")
    List<Item> lovItemPart(String companyCode, Integer plantNo, String partNo, String itemNo);

    @Query(value = "select itemNo, partNo from ITEM where company_code = :companyCode " +
            "and plant_no = :plantNo and item_no like %:itemNo% and source in ('B','C')")
    List<Item> getItemAndPartNoByItemNo(String companyCode, Integer plantNo, String itemNo);

    @Query(value = "select itemNo, partNo from ITEM where company_code = :companyCode " +
            "and plant_no = :plantNo and part_no like %:partNo% and source in ('B','C')")
    List<Item> getItemAndPartNoByPartNo(String companyCode, Integer plantNo, String partNo);

    @Query(value = "select i.partNo, i.itemNo, substring(i.description, 1, 60), i.loc, i.uom " +
            "from ITEM i where company_code = :companyCode and plant_no = :plantNo and item_no = :itemNo")
    List<Item> itemInfo(String companyCode, Integer plantNo, String itemNo);

    @Query(value = "select source from ITEM where company_code = :companyCode and plant_no = :plantNo " +
            "and item_no = :itemNo")
    Item getSource(String companyCode, Integer plantNo, String itemNo);
}
