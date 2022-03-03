package com.sunright.inventory.repository;

import com.sunright.inventory.entity.base.BaseIdEntity;
import com.sunright.inventory.entity.company.Company;
import com.sunright.inventory.entity.company.CompanyProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, BaseIdEntity>, JpaSpecificationExecutor<Company> {

    @Query("select concat(c.companyName,'-',c.plantName) as companyName " +
            "from COMPANY c where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo")
    CompanyProjection getCompanyAndPlantName(String companyCode, Integer plantNo);

    @Query("select c.stockLoc as stockLoc from COMPANY c where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo")
    CompanyProjection getStockLoc(String companyCode, Integer plantNo);

    @Query("select c.accessCa as accessCa from COMPANY c where c.id.companyCode = :companyCode and c.id.plantNo = :plantNo")
    CompanyProjection getAccCa(String companyCode, Integer plantNo);
}
