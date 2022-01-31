package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSRDetail;
import com.sunright.inventory.entity.msr.MSRDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsrDetailRepository extends JpaRepository<MSRDetail, Long> {

    @Query(value = "SELECT mdet.seqNo as seqNo, mdet.partNo as partNo, mdet.itemNo as itemNo FROM MSRDET mdet WHERE mdet.companyCode = :companyCode " +
            "AND mdet.plantNo = :plantNo AND mdet.msrNo = :msrNo AND (mdet.partNo LIKE %:partNo% " +
            "or mdet.itemNo LIKE %:itemNo%) AND COALESCE(mdet.retnQty,0) > COALESCE(mdet.recdQty,0)")
    MSRDetailProjection showLovPartNo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo);

    @Query(value = "SELECT sd.partNo as partNo, sd.seqNo as seqNo, sd.itemNo as itemNo, substring(COALESCE(i.description, sd.remarks), 1, 60) as description, " +
            "i.mslCode as mslCode, sd.itemType as itemType, sd.loc as loc, sd.uom as uom, sd.projectNo as projectNo, sd.grnNo as grnNo, " +
            "(COALESCE(sd.retnQty, 0) - COALESCE(sd.recdQty, 0)) as retnQty, sd.retnPrice as retnPrice " +
            "FROM MSRDET sd join ITEM i on i.companyCode = :companyCode AND i.plantNo = :plantNo AND i.itemNo = sd.itemNo WHERE " +
            "sd.companyCode = :companyCode AND sd.plantNo = :plantNo AND ((:msrSeqNo is null) or " +
            "(sd.seqNo IS NOT NULL AND sd.seqNo = :msrSeqNo)) AND sd.msrNo = :msrNo AND (sd.partNo LIKE %:partNo% " +
            "OR sd.itemNo LIKE %:itemNo%) AND COALESCE(sd.retnQty, 0) > COALESCE(sd.recdQty, 0)")
    MSRDetailProjection itemInfo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo, Integer msrSeqNo);

    @Query(value = "SELECT COUNT(md) as countItemNo FROM MSRDET md WHERE md.companyCode = :companyCode AND md.plantNo = :plantNo " +
            "AND md.msrNo = :msrNo AND md.itemNo LIKE %:itemNo% AND COALESCE(md.retnQty,0) > COALESCE(md.recdQty,0)")
    MSRDetailProjection getCountMsrByItemNo(String companyCode, Integer plantNo, String msrNo, String itemNo);

    @Query(value = "SELECT COUNT(md) as countPartNo FROM MSRDET md WHERE md.companyCode = :companyCode AND md.plantNo = :plantNo " +
            "AND md.msrNo = :msrNo AND md.partNo LIKE %:partNo% AND COALESCE(md.retnQty,0) > COALESCE(md.recdQty,0)")
    MSRDetailProjection getCountMsrByPartNo(String companyCode, Integer plantNo, String msrNo, String partNo);

    /*@Query(value = "SELECT (retnQty - COALESCE(recdQty, 0)) FROM MSRDET " +
            "WHERE company_code = :companyCode AND plant_no = :plantNo AND msr_no = :msrNo " +
            "AND COALESCE(item_no, 'X') = COALESCE(:itemNo, 'X') AND seq_no = :seqNo")
    List<MSRDetail> getRecdQtyByMsrNo(String companyCode, Integer plantNo, String msrNo, String itemNo, int seqNo);*/
}
