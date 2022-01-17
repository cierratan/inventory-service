package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pur.Pur;
import com.sunright.inventory.entity.pur.PurId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurRepository extends PagingAndSortingRepository<Pur, PurId>, JpaSpecificationExecutor<Pur> {

    @Query(value = "select seq_no, part_no, item_no, rec_seq from purdet where company_code = :companyCode " +
            "and plant_no = :plantNo and po_no = :poNo and (part_no like '%' || '%' || '%' or item_no like '%' || '%' || '%') " +
            "and nvl(order_qty,0) > nvl(recd_qty,0)", nativeQuery = true)
    List<Object[]> getDataFromPartNo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select pd.order_qty, substr(nvl(i.description, pd.remarks),1,60), pd.due_date, i.msl_code," +
            "nvl(pd.order_qty,0) order_qtys, pd.inv_uom, pd.std_pack_qty from purdet pd, item i where i.company_code(+) = pd.company_code " +
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
    List<Object[]> getDataFromItemAndPartNo(String companyCode, Integer plantNo, String poNo, String itemNo, String partNo, Integer poRecSeq);

    @Query(value = "select po_no,decode(open_close,'A', decode(s1.in_transit, 0, 'C', 'A'),open_close) open_close " +
            "from pur, (select sum(order_qty - nvl(recd_qty,0)) in_transit from purdet " +
            "where company_code = :companyCode and plant_no = :plantNo and po_no = :poNo) s1 where company_code = :companyCode " +
            "and plant_no = :plantNo and po_no = :poNo", nativeQuery = true)
    List<Object[]> checkStatusPoNoPur(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select supplier_code, currency_code, currency_rate, buyer, rlse_date, " +
            "regexp_replace(remarks,'[[:space:]~!@$%^&*_+=\\\"]',' ') remarks from pur " +
            "where company_code = :companyCode and plant_no = :plantNo and po_no = :poNo", nativeQuery = true)
    List<Object[]> getPurInfo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "SELECT po_no FROM pur WHERE company_code = :companyCode " +
            "AND plant_no = :plantNo AND nvl(open_close,'X') NOT IN ('V','Y') ORDER BY po_no", nativeQuery = true)
    List<Object[]> getAllPoNo(String companyCode, Integer plantNo);

    @Query(value = "select p.po_no, pd.rec_seq from pur p, purdet pd where pd.company_code(+) = p.company_code " +
            "and pd.plant_no(+) = p.plant_no and (:itemType = 0 and item_no(+) = :itemNo) and pd.po_no(+) = p.po_no " +
            "and p.company_code = :companyCode and p.plant_no = :plantNo and p.po_no = :poNo union select p.po_no, pd.rec_seq " +
            "from pur p, purdet pd where pd.company_code(+) = p.company_code and pd.plant_no(+) = p.plant_no " +
            "and (:itemType = 1 and part_no(+) = :partNo) and pd.po_no(+) = p.po_no and p.company_code = :companyCode " +
            "and p.plant_no = :plantNo and p.po_no = :poNo", nativeQuery = true)
    List<Object[]> getPoNoAndRecSeq(String companyCode, Integer plantNo, Integer itemType, String itemNo, String partNo, String poNo);
}
