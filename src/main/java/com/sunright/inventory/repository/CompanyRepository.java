package com.sunright.inventory.repository;

import com.sunright.inventory.entity.BaseIdEntity;
import com.sunright.inventory.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, BaseIdEntity>, JpaSpecificationExecutor<Company> {

    @Query(value = "select c.company_name || ' - ' || c.plant_name " +
            "from company c where c.company_code = :companyCode and c.plant_no = :plantNo", nativeQuery = true)
    List<Object[]> companyAndPlantName(String companyCode, Integer plantNo);

    Optional<Company> findCompanyById_CompanyCodeAndId_PlantNo(String companyCode, Integer plantNo);
}
