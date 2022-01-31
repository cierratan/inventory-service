package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pur.PurDet;
import com.sunright.inventory.entity.pur.PurDetId;
import com.sunright.inventory.entity.pur.PurDetProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurDetRepository extends JpaRepository<PurDet, PurDetId>, JpaSpecificationExecutor<PurDet> {

    @Query("SELECT COUNT(pd) as countItemNo FROM PURDET pd WHERE pd.id.companyCode = :companyCode " +
            "AND pd.id.plantNo = :plantNo AND pd.id.poNo = :poNo AND pd.itemNo LIKE %:itemNo% " +
            "AND COALESCE(pd.orderQty,0) > COALESCE(pd.recdQty,0)")
    PurDetProjection countItemNo(String companyCode, Integer plantNo, String poNo, String itemNo);

    @Query("SELECT COUNT(pd) as countPartNo FROM PURDET pd WHERE pd.id.companyCode = :companyCode " +
            "AND pd.id.plantNo = :plantNo AND pd.id.poNo = :poNo AND pd.partNo LIKE %:partNo% " +
            "AND COALESCE(pd.orderQty,0) > COALESCE(pd.recdQty,0)")
    PurDetProjection countPartNo(String companyCode, Integer plantNo, String poNo, String partNo);

    @Query("SELECT p.seqNo as seqNo, p.partNo as partNo, p.itemNo as itemNo, p.id.recSeq as recSeq FROM PURDET p " +
            "WHERE p.id.companyCode = :companyCode AND p.id.plantNo = :plantNo AND p.id.poNo = :poNo " +
            "AND (p.partNo LIKE %:partNo% or p.itemNo LIKE %:itemNo%) AND COALESCE(p.orderQty,0) > COALESCE(p.recdQty,0)")
    List<PurDetProjection> getDataFromPartNo(String companyCode, Integer plantNo, String poNo, String partNo, String itemNo);

    @Query("SELECT pd.id.recSeq as recSeq, pd.partNo as partNo FROM PURDET pd WHERE pd.id.companyCode = :companyCode " +
            "AND pd.id.plantNo = :plantNo AND pd.id.poNo = :poNo AND pd.partNo LIKE %:partNo% " +
            "AND (pd.id.recSeq = :poRecSeq OR :poRecSeq is null) " +
            "AND COALESCE(pd.orderQty,0) > COALESCE(pd.recdQty,0)")
    PurDetProjection checkDuplicatePartNo(String companyCode, Integer plantNo, String poNo, String partNo, Integer poRecSeq);

    @Query("SELECT pd.partNo as partNo, pd.id.recSeq as recSeq, pd.itemNo as itemNo, (SUBSTRING(COALESCE(i.description,pd.remarks),1,60)) as description, " +
            "i.mslCode as mslCode, pd.itemType as itemType, pd.loc as loc, pd.uom as uom, pd.projectNo as projectNo, " +
            "(COALESCE(pd.orderQty,0) - COALESCE(pd.recdQty,0)) as orderQty, pd.unitPrice as unitPrice, " +
            "pd.dueDate as dueDate, pd.resvQty as resvQty, pd.invUom as invUom, pd.stdPackQty as stdPackQty, pd.remarks as remarks " +
            "FROM PURDET pd left join ITEM i on i.companyCode = pd.id.companyCode AND i.plantNo = pd.id.plantNo AND i.itemNo = pd.itemNo " +
            "WHERE pd.id.companyCode =:companyCode AND pd.id.plantNo =:plantNo AND pd.id.poNo =:poNo " +
            "AND (pd.itemNo LIKE %:itemNo% OR pd.partNo LIKE %:partNo%) AND (pd.id.recSeq =:poRecSeq " +
            "OR :poRecSeq IS NULL ) AND COALESCE(pd.orderQty,0) > COALESCE(pd.recdQty,0)")
    PurDetProjection getDataFromItemAndPartNo(String companyCode, Integer plantNo, String poNo, String itemNo, String partNo, Integer poRecSeq);

    @Query("SELECT p.id.poNo as poNo, pd.id.recSeq as recSeq FROM PUR p left join PURDET pd on pd.id.companyCode = p.id.companyCode " +
            "AND pd.id.plantNo = p.id.plantNo AND pd.id.poNo = p.id.poNo WHERE (:itemType = 0 AND pd.itemNo = :itemNo OR :itemType = 1 AND pd.partNo = :partNo)  " +
            "AND p.id.companyCode = :companyCode AND p.id.plantNo = :plantNo AND p.id.poNo = :poNo")
    PurDetProjection getPoNoAndRecSeq(String companyCode, Integer plantNo, Integer itemType, String itemNo, String partNo, String poNo);
}
