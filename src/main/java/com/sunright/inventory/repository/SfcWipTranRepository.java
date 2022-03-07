package com.sunright.inventory.repository;

import com.sunright.inventory.entity.sfcwip.SfcWipTran;
import com.sunright.inventory.entity.sfcwip.SfcWipTranId;
import com.sunright.inventory.entity.sfcwip.SfcWipTranProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SfcWipTranRepository extends JpaRepository<SfcWipTran, SfcWipTranId> {

    @Query(value = "select tg.cnt as cnt, tc.product_id, tc.project_no_sub, tc.pcb_part_no, tc.seq_no, tc.status " +
            "    from sfc_wip_tran tc, " +
            "     ( select count(*) as cnt, t.project_no_sub, t.pcb_part_no " +
            "    from sfc_wip_tran t " +
            "    group by t.project_no_sub, t.pcb_part_no ) tg " +
            "where tg.project_no_sub = tc.project_no_sub " +
            "    and tg.pcb_part_no = tc.pcb_part_no " +
            "    and tc.project_no_sub = :projectNo " +
            "    and tc.pcb_part_no = :partNo " +
            "order by tc.product_id ", nativeQuery = true)
    List<SfcWipTranProjection> getSfcWipTran(String projectNo, String partNo);
}
