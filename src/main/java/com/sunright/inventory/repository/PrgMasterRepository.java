package com.sunright.inventory.repository;

import com.sunright.inventory.entity.prgmaster.PrgMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrgMasterRepository extends JpaRepository<PrgMaster, PrgMaster>, JpaSpecificationExecutor<PrgMaster> {

    Optional<PrgMaster> findPrgMasterByPrgIdAndModuleCd(String prgId, String moduleCd);
}
