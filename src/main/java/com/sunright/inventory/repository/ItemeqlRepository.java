package com.sunright.inventory.repository;

import com.sunright.inventory.entity.itemeql.Itemeql;
import com.sunright.inventory.entity.itemeql.ItemeqlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemeqlRepository extends JpaRepository<Itemeql, ItemeqlId>, JpaSpecificationExecutor<Itemeql> {
    Itemeql findItemeqlByIdCompanyCodeAndIdPlantNoAndIdItemNo(String companyCode, Integer plantNo, String itemNo);

    Itemeql findItemeqlByIdCompanyCodeAndIdPlantNoAndIdAlternate(String companyCode, Integer plantNo, String itemNo);

    void deleteItemeqlByIdCompanyCodeAndIdPlantNoAndIdItemNo(String companyCode, Integer plantNo, String itemNo);

    void deleteItemeqlByIdCompanyCodeAndIdPlantNoAndIdAlternate(String companyCode, Integer plantNo, String itemNo);
}
