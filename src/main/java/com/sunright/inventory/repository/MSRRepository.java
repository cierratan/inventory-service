package com.sunright.inventory.repository;

import com.sunright.inventory.entity.msr.MSR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MSRRepository extends JpaRepository<MSR, Long>, JpaSpecificationExecutor<MSR> {

    @Query(value = "select seq_no, part_no, item_no from msrdet where company_code = :companyCode " +
            "and plant_no = :plantNo and msr_no = :msrNo and (part_no like '%' || :partNo || '%' " +
            "or item_no like '%' || :itemNo || '%') and nvl(retn_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> showLovPartNo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo);

    @Query(value = "select sd.part_no, sd.seq_no, sd.item_no, substr(nvl(i.description, sd.remarks), 1, 60) description, " +
            "i.msl_code, sd.item_type, sd.loc, sd.uom, sd.project_no, sd.grn_no, nvl(sd.retn_qty, 0) - nvl(sd.recd_qty, 0) retn_qty, " +
            "sd.retn_price from msrdet sd, item i where i.company_code(+) = :companyCode and i.plant_no(+) = :plantNo and i.item_no(+) = sd.item_no " +
            "and sd.company_code = :companyCode and sd.plant_no = :plantNo and ((:msrSeqNo is null) or " +
            "(sd.seq_no is not null and sd.seq_no = :msrSeqNo)) and sd.msr_no = :msrNo and (sd.part_no like '%' || :partNo || '%' " +
            "or sd.item_no like '%' || :itemNo || '%') and nvl(sd.retn_qty, 0) > nvl(sd.recd_qty, 0)", nativeQuery = true)
    List<Object[]> itemInfo(String companyCode, Integer plantNo, String msrNo, String partNo, String itemNo, Integer msrSeqNo);

    @Query(value = "select count(*) from msrdet where company_code = :companyCode and plant_no = :plantNo " +
            "and msr_no = :msrNo and item_no like '%'||:itemNo||'%' and nvl(retn_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> getCountMsrByItemNo(String companyCode, Integer plantNo, String msrNo, String itemNo);

    @Query(value = "select count(*) from msrdet where company_code = :companyCode and plant_no = :plantNo " +
            "and msr_no = :msrNo and part_no like '%'||:partNo||'%' and nvl(retn_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> getCountMsrByPartNo(String companyCode, Integer plantNo, String msrNo, String partNo);

    Optional<MSR> findMSRByCompanyCodeAndPlantNoAndMsrNo(String companyCode, Integer plantNo, String msrNo);
}
