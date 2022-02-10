package com.sunright.inventory.repository;

import com.sunright.inventory.entity.draftpur.DraftPur;
import com.sunright.inventory.entity.draftpur.DraftPurId;
import com.sunright.inventory.entity.pur.DraftPurProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface DraftPurRepository extends JpaRepository<DraftPur, DraftPurId>, JpaSpecificationExecutor<DraftPur> {

    @Query("SELECT dp.id.poNo as poNo, COALESCE(dp.openClose,'O') as openClose FROM DRAFT_PUR dp WHERE dp.id.companyCode = :companyCode " +
            "AND dp.id.plantNo = :plantNo AND dp.id.poNo = :poNo")
    DraftPurProjection checkStatusPoNoDraftPur(String companyCode, Integer plantNo, String poNo);

    @Modifying
    @Query("UPDATE DRAFT_PUR dp set dp.openClose = 'C', dp.closeDate = :closeDate " +
            "WHERE dp.id.companyCode = :companyCode AND dp.id.plantNo = :plantNo AND dp.id.poNo = :poNo")
    void updatePOClose(String companyCode, Integer plantNo, String poNo, Date closeDate);
}
