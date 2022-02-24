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

    @Query(value = "select c.docm_no as docmNo, c.docm_type as docmType, cd.rec_seq as recSeq, nvl(cd.docm_qty, 0) as docmQty, nvl(max(cs.seq_no), 0) as seqNo " +
            "from coq c," +
            "     coq_det cd," +
            "     coq_det_sub cs " +
            "where cs.company_code(+) = :companyCode" +
            "  and cs.plant_no(+) = :plantNo" +
            "  and cs.det_rec_seq(+) = cd.rec_seq" +
            "  and cs.docm_no(+) = cd.docm_no" +
            "  and cd.company_code(+) = :companyCode" +
            "  and cd.plant_no(+) = :plantNo" +
            "  and cd.item_no(+) = :itemNo" +
            "  and cd.item_type(+) = 0" +
            "  and cd.docm_no(+) = c.docm_no" +
            "  and c.company_code = :companyCode" +
            "  and c.plant_no = :plantNo" +
            "  and c.docm_type in (:WKType)" +
            "  and c.project_no_sub = :projectNo " +
            "group by c.docm_no, c.docm_type, cd.rec_seq, nvl(cd.docm_qty, 0) " +
            "union " +
            "select c.docm_no as docmNo, c.docm_type as docmType, cd.rec_seq as recSeq, nvl(cd.docm_qty, 0) as docmQty, nvl(max(cs.seq_no), 0) as seqNo " +
            "from coq c," +
            "     coq_det cd," +
            "     coq_det_sub cs " +
            "where cs.company_code(+) = :companyCode" +
            "  and cs.plant_no(+) = :plantNo" +
            "  and cs.det_rec_seq(+) = cd.rec_seq" +
            "  and cs.docm_no(+) = cd.docm_no" +
            "  and cd.company_code(+) = :companyCode" +
            "  and cd.plant_no(+) = :plantNo" +
            "  and cd.item_no(+) = :itemNo" +
            "  and cd.item_type(+) = :plantNo" +
            "  and cd.docm_no(+) = c.docm_no" +
            "  and c.company_code = :companyCode" +
            "  and c.plant_no = :plantNo" +
            "  and c.docm_type in (:PRType)" +
            "  and c.docm_no = :projectNo " +
            "group by c.docm_no, c.docm_type, cd.rec_seq, nvl(cd.docm_qty, 0)", nativeQuery = true)
    COQProjection coqRecM(String companyCode, Integer plantNo, String itemNo, String projectNo, String WKType, String PRType);

    @Query("select c.id.docmNo as docmNo, c.docmType as docmType, cd.id.recSeq as recSeq, coalesce(cd.docmQty, 0) as docmQty, coalesce(max(cs.id.seqNo), 0) as seqNo " +
            "from COQ c left join COQ_DET cd on cd.id.docmNo = c.id.docmNo " +
            "left join COQ_DET_SUB cs on cs.id.detRecSeq = cd.id.recSeq and cs.id.docmNo = cd.id.docmNo " +
            "where cs.id.companyCode = :companyCode" +
            "  and cs.id.plantNo = :plantNo" +
            "  and cd.id.companyCode = :companyCode" +
            "  and cd.id.plantNo = :plantNo" +
            "  and cd.itemNo = :itemNo" +
            "  and cd.itemType = 0" +
            "  and c.id.companyCode = :companyCode" +
            "  and c.id.plantNo = :plantNo" +
            "  and c.docmType in (:WKType)" +
            "  and c.projectNoSub = :projectNo " +
            "group by c.id.docmNo, c.docmType, cd.id.recSeq, coalesce(cd.docmQty,0)")
    COQProjection coqRecN(String companyCode, Integer plantNo, String itemNo, String projectNo, String WKType);
}
