package com.sunright.inventory.repository;

import com.sunright.inventory.entity.enums.Status;
import com.sunright.inventory.entity.mrv.MRV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MRVRepository extends JpaRepository<MRV, Long>, JpaSpecificationExecutor<MRV> {

    MRV findByCompanyCodeAndPlantNoAndAndMrvNoAndStatus(String companyCode, Integer plantNo, String mrvNo, Status status);
}
