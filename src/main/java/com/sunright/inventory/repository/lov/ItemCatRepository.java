package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCatRepository extends JpaRepository<ItemCat, ItemCatId>, JpaSpecificationExecutor<ItemCat> {
    @Query("SELECT DISTINCT item.id.categoryCode as categoryCode, CONCAT(item.id.categoryCode, '-', item.description) as description " +
            "FROM ITEMCAT item join CODE_MAP map on map.id.companyCode = item.id.companyCode " +
            "and map.id.plantNo = item.id.plantNo and map.id.mapTo = item.id.categoryGroup " +
            "WHERE map.id.mapType = :mapType and map.id.mapFrom = :mapFrom and map.id.companyCode = :companyCode " +
            "and map.id.plantNo = :plantNo")
    List<ItemCatProjection> findItemCatBy(String mapType, String mapFrom, String companyCode, Integer plantNo, Sort sort);

    @Query("SELECT DISTINCT item.id.categorySubCode as categorySubCode, CONCAT(item.id.categorySubCode, '-', item.subDescription) as subDescription " +
            "FROM ITEMCAT item join CODE_MAP map on map.id.companyCode = item.id.companyCode " +
            "and map.id.plantNo = item.id.plantNo and map.id.mapTo = item.id.categoryGroup " +
            "WHERE item.id.categoryCode = :categoryCode and map.id.mapType = :mapType " +
            "and map.id.mapFrom = :mapFrom and item.id.companyCode = :companyCode and item.id.plantNo = :plantNo ")
    List<CategorySubProjection> findSubCatBy(String categoryCode, String mapType, String mapFrom, String companyCode, Integer plantNo, Sort sort);

    @Query("SELECT code FROM CODE_DESC code WHERE code.id.type = :type ")
    List<CodeDesc> findCodeDescBy(String type, Sort sort);
}
