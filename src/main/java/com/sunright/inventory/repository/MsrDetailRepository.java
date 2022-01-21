package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSRDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MsrDetailRepository extends JpaRepository<MSRDetail, Long> {

    @Query(value = "select seqNo, partNo, itemNo from MSRDET where company_code = :companyCode " +
            "and plant_no = :plantNo and msr_no = :msrNo and (part_no like %:partNo% " +
            "or item_no like %:itemNo%) and coalesce(retn_qty,0) > coalesce(recd_qty,0)")
    List<MSRDetail> showLovPartNo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo);

    @Query(value = "select sd.partNo, sd.seqNo, sd.itemNo, substring(coalesce(i.description, sd.remarks), 1, 60), " +
            "i.mslCode, sd.itemType, sd.loc, sd.uom, sd.projectNo, sd.grnNo, coalesce(sd.retnQty, 0) - coalesce(sd.recdQty, 0), " +
            "sd.retnPrice from MSRDET sd, ITEM i where i.companyCode = :companyCode and i.plantNo = :plantNo and i.itemNo = sd.itemNo " +
            "and sd.companyCode = :companyCode and sd.plantNo = :plantNo and ((:msrSeqNo is null) or " +
            "(sd.seqNo is not null and sd.seqNo = :msrSeqNo)) and sd.msrNo = :msrNo and (sd.partNo like %:partNo% " +
            "or sd.itemNo like %:itemNo%) and coalesce(sd.retnQty, 0) > coalesce(sd.recdQty, 0)")
    List<MSRDetail> itemInfo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo, Integer msrSeqNo);

    @Query(value = "select count(md) from MSRDET md where company_code = :companyCode and plant_no = :plantNo " +
            "and msr_no = :msrNo and item_no like %:itemNo% and coalesce(retn_qty,0) > coalesce(recd_qty,0)")
    Long countMsrByItemNo(String companyCode, Integer plantNo, String msrNo, String itemNo);

    @Query(value = "select count(md) from MSRDET md where company_code = :companyCode and plant_no = :plantNo " +
            "and msr_no = :msrNo and part_no like %:partNo% and coalesce(retn_qty,0) > coalesce(recd_qty,0)")
    Long countMsrByPartNo(String companyCode, Integer plantNo, String msrNo, String partNo);

    @Query(value = "select (retnQty - coalesce(recdQty, 0)) from MSRDET " +
            "where company_code = :companyCode and plant_no = :plantNo and msr_no = :msrNo " +
            "and coalesce(item_no, 'X') = coalesce(:itemNo, 'X') and seq_no = :seqNo")
    List<MSRDetail> getRecdQtyByMsrNo(String companyCode, Integer plantNo, String msrNo, String itemNo, int seqNo);
}
