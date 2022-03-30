package com.sunright.inventory.repository;

import com.sunright.inventory.entity.draftpur.DraftPurDet;
import com.sunright.inventory.entity.draftpur.DraftPurDetId;
import com.sunright.inventory.entity.draftpur.DraftPurDetProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Repository
public interface DraftPurDetRepository extends JpaRepository<DraftPurDet, DraftPurDetId>, JpaSpecificationExecutor<DraftPurDet> {

    @Modifying
    @Query("UPDATE DRAFT_PURDET d set d.resvQty = :resvQty WHERE d.id.companyCode = :companyCode " +
            "AND d.id.plantNo = :plantNo AND d.id.poNo = :poNo AND d.itemNo = :itemNo AND d.id.recSeq = :seqNo")
    void updateResvQty(BigDecimal resvQty, String companyCode, Integer plantNo, String poNo, String itemNo, Integer seqNo);

    @Modifying
    @Query("UPDATE DRAFT_PURDET d set d.rlseDate = :rlseDate, d.recdDate = :recdDate, d.rlseQty = :rlseQty, " +
            "d.recdQty = :recdQty, d.recdPrice = :poPrice WHERE d.id.companyCode = :companyCode " +
            "AND d.id.plantNo = :plantNo AND d.itemNo = :itemNo AND d.projectNo = :projectNo")
    void updateRlseRecdDateRlseRecdQtyRecdPrice(Date rlseDate, Date recdDate, BigDecimal rlseQty, BigDecimal recdQty,
                                                BigDecimal poPrice, String companyCode, Integer plantNo,
                                                String itemNo, String projectNo);

    @Query("select coalesce(d.resvQty,0) as resvQty, coalesce(d.rlseQty,0) as rlseQty, coalesce(d.recdQty,0) as recdQty " +
            "from DRAFT_PURDET d where d.id.companyCode = :companyCode and d.id.plantNo = :plantNo and d.itemNo = :itemNo " +
            "and d.projectNo = :projectNo")
    DraftPurDetProjection draftPurDetInfo(String companyCode, Integer plantNo, String itemNo, String projectNo);
}
