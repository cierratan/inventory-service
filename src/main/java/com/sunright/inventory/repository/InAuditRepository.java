package com.sunright.inventory.repository;

import com.sunright.inventory.entity.inaudit.InAudit;
import com.sunright.inventory.entity.inaudit.InAuditProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InAuditRepository extends JpaRepository<InAudit, Long> {

    @Query("select count(ia) as countInAudit from INAUDIT ia where ia.companyCode = :companyCode " +
            "and ia.plantNo = :plantNo and ia.itemNo = :itemNo")
    InAuditProjection inauditCur(String companyCode, Integer plantNo, String itemNo);
}
