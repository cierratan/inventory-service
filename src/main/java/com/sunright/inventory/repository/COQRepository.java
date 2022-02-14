package com.sunright.inventory.repository;

import com.sunright.inventory.entity.coq.COQ;
import com.sunright.inventory.entity.coq.COQId;
import com.sunright.inventory.entity.coq.COQProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface COQRepository extends JpaRepository<COQ, COQId>, JpaSpecificationExecutor<COQ> {

    @Query("SELECT c.id.docmNo as docmNo, cd.id.recSeq as recSeq, coalesce(cd.docmQty,0) as docmQty, " +
            "max (coalesce(cs.id.seqNo,0)) as seqNo FROM COQ c LEFT JOIN COQ_DET cd on cd.id.docmNo = c.id.docmNo " +
            "LEFT JOIN COQ_DET_SUB cs on cs.id.detRecSeq = cd.id.recSeq AND cs.id.docmNo = cd.id.docmNo " +
            "WHERE cs.id.companyCode = :companyCode AND cs.id.plantNo = :plantNo AND cd.id.companyCode = :companyCode " +
            "AND cd.id.plantNo = :plantNo AND cd.itemNo = :itemNo AND cd.itemType = 0 AND c.id.companyCode = :companyCode " +
            "AND c.id.plantNo = :plantNo AND c.docmType in ('WO') AND c.projectNoSub = :projectNo GROUP BY c.id.docmNo, " +
            "cd.id.recSeq, coalesce(cd.docmQty,0)")
    COQProjection coqRec(String companyCode, Integer plantNo, String itemNo, String projectNo);
}
