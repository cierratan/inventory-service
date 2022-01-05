package com.sunright.inventory.repository;

import com.sunright.inventory.entity.PurDet;
import com.sunright.inventory.entity.PurDetId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurDetRepository extends PagingAndSortingRepository<PurDet, PurDetId>, JpaSpecificationExecutor<PurDet> {

    @Query(value = "select pd.order_qty, substr(nvl(i.description, pd.remarks),1,60), pd.due_date, i.msl_code," +
            "nvl(pd.order_qty,0) order_qty, pd.inv_uom, pd.std_pack_qty from purdet pd, item i where i.company_code(+) = pd.company_code " +
            "and i.plant_no(+) = pd.plant_no and i.item_no(+) = pd.item_no " +
            "and pd.company_code = :companyCode and pd.plant_no = :plantNo and pd.po_no = :poNo", nativeQuery = true)
    List<Object[]> getPurDetInfo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select code_desc from default_code c, default_code_det d where d.company_code = c.company_code " +
            "and d.plant_no = c.plant_no and d.default_code = c.default_code and d.code_value = :uom " +
            "and c.company_code = :companyCode and c.plant_no = :plantNo and c.default_code = 'INVENTORY.UOM' and c.db_status = 'Y'", nativeQuery = true)
    List<Object[]> getUomDesc(String companyCode, Integer plantNo, String uom);

    @Query(value = "select count(*) from purdet where company_code = :companyCode " +
            "and plant_no = :plantNo and po_no = :poNo and item_no like '%'||:itemNo||'%' " +
            "and nvl(order_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> checkItemNo(String companyCode, Integer plantNo, String poNo, String itemNo);

    @Query(value = "select count(*) from purdet where company_code = :companyCode " +
            "and plant_no = :plantNo and po_no = :poNo and part_no like '%'||:partNo||'%' " +
            "and nvl(order_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> checkPartNo(String companyCode, Integer plantNo, String poNo, String partNo);

    @Query(value = "select pd.rec_seq, pd.part_no from purdet pd where company_code = :companyCode " +
            "and plant_no = :plantNo and pd.po_no = :poNo and pd.part_no like '%'||:partNo||'%' " +
            "and (pd.rec_seq = :poRecSeq or :poRecSeq is null) " +
            "and nvl(pd.order_qty,0) > nvl(pd.recd_qty,0)", nativeQuery = true)
    List<Object[]> checkDuplicatePartNo(String companyCode, Integer plantNo, String poNo, String partNo, Integer poRecSeq);

    @Query(value = "SELECT pd.part_no,pd.rec_seq,pd.item_no,substr(nvl(i.description,pd.remarks),1,60)," +
            "i.msl_code,pd.item_type,pd.loc,pd.uom,pd.project_no,nvl(pd.order_qty,0) - nvl(pd.recd_qty,0) order_qty,pd.unit_price," +
            "pd.due_date,pd.resv_qty,pd.inv_uom,pd.std_pack_qty,pd.remarks FROM purdet pd, item i " +
            "WHERE i.company_code (+) = pd.company_code AND i.plant_no (+) = pd.plant_no AND i.item_no (+) = pd.item_no " +
            "AND pd.company_code =:companyCode AND pd.plant_no =:plantNo AND pd.po_no =:poNo " +
            "AND ( (:itemNo IS NULL ) AND ( pd.part_no LIKE '%' ||:partNo|| '%' ) " +
            "OR (:partNo IS NULL ) AND ( pd.item_no LIKE '%' ||:itemNo || '%' ) OR ( pd.item_no LIKE '%' " +
            "||:itemNo || '%' ) AND ( pd.part_no LIKE '%' ||:partNo || '%' ) ) AND ( pd.rec_seq =:poRecSeq " +
            "OR :poRecSeq IS NULL ) AND nvl(pd.order_qty,0) > nvl(pd.recd_qty,0)", nativeQuery = true)
    List<Object[]> getItemInfo(String companyCode, Integer plantNo, String poNo, String itemNo, String partNo, Integer poRecSeq);
}
