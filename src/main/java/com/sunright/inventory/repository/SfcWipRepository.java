package com.sunright.inventory.repository;

import com.sunright.inventory.entity.sfcwip.SfcWip;
import com.sunright.inventory.entity.sfcwip.SfcWipId;
import com.sunright.inventory.entity.sfcwip.SfcWipProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface SfcWipRepository extends JpaRepository<SfcWip, SfcWipId>, JpaSpecificationExecutor<SfcWip> {

    @Query("SELECT s.id.projectNoSub as projectNoSub, s.id.pcbPartNo as pcbPartNo, s.pcbQty as pcbQty " +
            "FROM SFC_WIP s where s.id.projectNoSub = :projectNo and s.id.pcbPartNo = :partNo")
    SfcWipProjection wipCur(String projectNo, String partNo);

    @Query("SELECT s.id.projectNoSub as projectNoSub, s.id.pcbPartNo as pcbPartNo, s.pcbQty as pcbQty " +
            "FROM SFC_WIP s " +
            "WHERE s.id.projectNoSub = :projectNo and s.id.pcbPartNo = :partNo and s.status NOT IN ('C', 'V') ")
    SfcWipProjection wipCurWithStatusCheck(String projectNo, String partNo);

    @Modifying
    @Query("UPDATE SFC_WIP s set s.pcbQty = :pcbQty WHERE s.id.projectNoSub = :projectNo and s.id.pcbPartNo = :partNo")
    void updatePcbQty(BigDecimal pcbQty, String projectNo, String partNo);
}