package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bomproj.Bomproj;
import com.sunright.inventory.entity.bomproj.BomprojId;
import com.sunright.inventory.entity.bomproj.BomprojProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BomprojRepository extends JpaRepository<Bomproj, BomprojId>, JpaSpecificationExecutor<Bomproj> {

    @Query("SELECT b.pickedStatus as pickedStatus FROM BOMPROJ b WHERE b.id.companyCode = :companyCode " +
            "AND b.id.plantNo = :plantNo AND b.projectNo = :projectNo")
    BomprojProjection getPickedStatus(String companyCode, Integer plantNo, String projectNo);

    @Modifying
    @Query("UPDATE BOMPROJ b set b.pickedStatus = 'I', b.sivNo = :sivNo WHERE b.projectNo = :projectNo")
    void updatePickedStatusSivNo(String sivNo, String projectNo);

    @Modifying
    @Query("UPDATE BOMPROJ b set b.sivNo = :sivNo WHERE b.projectNo = :projectNo")
    void updateSivNo(String sivNo, String projectNo);
}
