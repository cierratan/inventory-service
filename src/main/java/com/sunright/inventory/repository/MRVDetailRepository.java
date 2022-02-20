package com.sunright.inventory.repository;

import com.sunright.inventory.entity.mrv.MRVDetail;
import com.sunright.inventory.entity.mrv.MRVDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MRVDetailRepository extends JpaRepository<MRVDetail, MRVDetailId>, JpaSpecificationExecutor<MRVDetail> {

    @Query("SELECT md.projectNo as projectNo FROM MRVDET md WHERE md.id.companyCode = :companyCode AND md.id.plantNo = :plantNo " +
            "AND md.id.mrvNo = :mrvNo AND md.itemNo = :itemNo AND md.id.seqNo = :seqNo")
    MRVDetailProjection mrvDetCur(String companyCode, Integer plantNo, String mrvNo, String itemNo, int seqNo);
}
