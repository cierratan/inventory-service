package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.GrnDet;
import com.sunright.inventory.entity.grn.GrnDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GrnDetRepository extends JpaRepository<GrnDet, Long>, JpaSpecificationExecutor<GrnDet> {

    @Query("SELECT DISTINCT g.grnNo as grnNo, g.seqNo as seqNo, g.uom as uom, g.issuedQty as issuedQty " +
            "FROM GRNDET g join ITEMBATC i ON g.companyCode = i.id.companyCode AND g.plantNo = i.id.plantNo " +
            "AND g.grnNo = i.grnNo AND g.seqNo = i.grnSeq AND g.itemNo = i.id.itemNo " +
            "WHERE i.id.companyCode = :companyCode AND i.id.plantNo = :plantNo " +
            "AND i.id.batchNo = :batchNo AND i.id.itemNo = :itemNo")
    List<GrnDetailProjection> getGrndetCur(String companyCode, Integer plantNo, Long batchNo, String itemNo);

    @Modifying
    @Query("UPDATE GRNDET g set g.sivNo = :sivNo, g.issuedQty = :issuedQty WHERE g.grnNo = :grnNo AND g.seqNo = :seqNo")
    void updateSivNoIssuedQty(String sivNo, BigDecimal issuedQty, String grnNo, Integer seqNo);
}
