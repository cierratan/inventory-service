package com.sunright.inventory.repository;

import com.sunright.inventory.entity.coq.COQDetail;
import com.sunright.inventory.entity.coq.COQDetailId;
import com.sunright.inventory.entity.coq.COQProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface COQDetailRepository extends JpaRepository<COQDetail, COQDetailId>, JpaSpecificationExecutor<COQDetail> {

    @Query("SELECT max(coalesce(cd.id.recSeq,0)) as recSeq, max(coalesce(cd.seqNo,0)) as seqNo FROM COQ_DET cd " +
            "WHERE cd.id.companyCode = :companyCode AND cd.id.plantNo = :plantNo AND cd.id.docmNo = :docmNo")
    COQProjection coqDet(String companyCode, Integer plantNo, String docmNo);

    @Modifying
    @Query("UPDATE COQ_DET cd set cd.docmQty = :docmQty WHERE cd.id.companyCode = :companyCode AND cd.id.plantNo = :plantNo " +
            "AND cd.id.recSeq = :recSeq AND cd.id.docmNo = :docmNo")
    void updateDocmQty(BigDecimal docmQty, String companyCode, Integer plantNo, Integer recSeq, String docmNo);
}
