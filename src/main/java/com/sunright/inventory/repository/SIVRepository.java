package com.sunright.inventory.repository;

import com.sunright.inventory.entity.siv.SIV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SIVRepository extends JpaRepository<SIV, Long>, JpaSpecificationExecutor<SIV> {
    Optional<SIV> findSIVByCompanyCodeAndPlantNoAndSivNo(String companyCode, Integer plantNo, String sivNo);
}
