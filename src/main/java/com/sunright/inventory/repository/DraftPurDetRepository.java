package com.sunright.inventory.repository;

import com.sunright.inventory.entity.draftpur.DraftPurDet;
import com.sunright.inventory.entity.draftpur.DraftPurDetId;
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
    @Query("UPDATE DRAFT_PURDET d set d.resvQty = (d.resvQty - :poResvQty) WHERE d.id.companyCode = :companyCode AND d.id.plantNo = :plantNo " +
            "AND d.id.poNo = :poNo AND d.itemNo = :itemNo AND d.id.recSeq = :seqNo")
    void updateResvQty(BigDecimal poResvQty, String companyCode, Integer plantNo, String poNo, String itemNo, Integer seqNo);

    @Modifying
    @Query("UPDATE DRAFT_PURDET d set d.rlseDate = :rlseDate, d.recdDate = :recdDate, d.rlseQty = (coalesce(d.rlseQty,0) + :recdQty1), " +
            "d.recdQty = (coalesce(d.recdQty,0) + :recdQty2), d.recdPrice = :poPrice WHERE d.id.companyCode = :companyCode " +
            "AND d.id.plantNo = :plantNo AND d.id.poNo = :poNo AND d.id.recSeq = :poRecSeq")
    void updateRlseRecdDateRlseRecdQtyRecdPrice(Date rlseDate, Date recdDate, BigDecimal recdQty1, BigDecimal recdQty2, BigDecimal poPrice,
                                                String companyCode, Integer plantNo, String poNo, Integer poRecSeq);
}
