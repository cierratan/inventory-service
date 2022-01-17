package com.sunright.inventory.repository.lov;

import com.sunright.inventory.entity.lov.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCatRepository extends JpaRepository<ItemCat, Long>, JpaSpecificationExecutor<ItemCat> {
    @Query("SELECT DISTINCT item.categoryCode as categoryCode, CONCAT(item.categoryCode, '-', item.description) as description " +
            "FROM ITEMCAT item join CODE_MAP map on map.id.companyCode = item.companyCode " +
            "and map.id.plantNo = item.plantNo and map.id.mapTo = item.categoryGroup " +
            "WHERE map.id.mapType = :mapType and map.id.mapFrom = :mapFrom and map.id.companyCode = :companyCode " +
            "and map.id.plantNo = :plantNo")
    List<ItemCatProjection> findItemCatBy(String mapType, String mapFrom, String companyCode, Integer plantNo, Sort sort);

    @Query("SELECT DISTINCT item.categorySubCode as categorySubCode, CONCAT(item.categorySubCode, '-', item.subDescription) as subDescription " +
            "FROM ITEMCAT item join CODE_MAP map on map.id.companyCode = item.companyCode " +
            "and map.id.plantNo = item.plantNo and map.id.mapTo = item.categoryGroup " +
            "WHERE item.categoryCode = :categoryCode and map.id.mapType = :mapType " +
            "and map.id.mapFrom = :mapFrom and item.companyCode = :companyCode and item.plantNo = :plantNo ")
    List<CategorySubProjection> findSubCatBy(String categoryCode, String mapType, String mapFrom, String companyCode, Integer plantNo, Sort sort);

    @Query("SELECT det.codeValue as codeValue, det.codeDesc as codeDesc " +
            "FROM DEFAULT_CODE_DET det " +
            "WHERE det.id.defaultCode = 'ITEMCAT.CAT_GRP' AND det.status = '1' " +
            "ORDER BY det.id.seqNo")
    List<ValueDescProjection> findCategoryGroups();

    @Query("SELECT code FROM CODE_DESC code WHERE code.id.type = :type ")
    List<CodeDesc> findCodeDescBy(String type, Sort sort);

    List<ItemCat> findByCompanyCodeAndPlantNoAndCategoryCodeAndCategorySubCodeAndCategoryGroup(String companyCode, Integer plantNo, String categoryCode, String categorySubCode, String categoryGroup);
}
