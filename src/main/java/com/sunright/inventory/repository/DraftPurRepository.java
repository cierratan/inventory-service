package com.sunright.inventory.repository;

import com.sunright.inventory.entity.draftpur.DraftPur;
import com.sunright.inventory.entity.draftpur.DraftPurId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DraftPurRepository extends JpaRepository<DraftPur, DraftPurId>, JpaSpecificationExecutor<DraftPur> {

    @Query(value = "select dp.id.poNo, coalesce(dp.openClose,'O') " +
            "from DRAFT_PUR dp where dp.id.companyCode = :companyCode and dp.id.plantNo = :plantNo and dp.id.poNo = :poNo")
    List<DraftPur> checkStatusPoNoDraftPur(String companyCode, Integer plantNo, String poNo);
}
