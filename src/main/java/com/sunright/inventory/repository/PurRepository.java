package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pur.Pur;
import com.sunright.inventory.entity.pur.PurDet;
import com.sunright.inventory.entity.pur.PurId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurRepository extends JpaRepository<Pur, PurId>, JpaSpecificationExecutor<Pur> {

    @Query(value = "select p.seqNo, p.partNo, p.itemNo, p.id.recSeq from PURDET p where p.id.companyCode = :companyCode " +
            "and p.id.plantNo = :plantNo and p.id.poNo = :poNo and (p.partNo like %:partNo% or p.itemNo like %:itemNo%) " +
            "and coalesce(p.orderQty,0) > coalesce(p.recdQty,0)")
    List<PurDet> getDataFromPartNo(String companyCode, Integer plantNo, String poNo, String partNo, String itemNo);

    /*@Query(value = "select pd.order_qty, substr(coalesce(i.description, pd.remarks),1,60), pd.due_date, i.msl_code," +
            "coalesce(pd.order_qty,0) order_qtys, pd.inv_uom, pd.std_pack_qty from PURDET pd, item i where i.id.companyCode(+) = pd.id.companyCode " +
            "and i.id.plantNo(+) = pd.id.plantNo and i.item_no(+) = pd.item_no " +
            "and pd.id.companyCode = :companyCode and pd.id.plantNo = :plantNo and pd.id.poNo = :poNo")
    List<Object[]> getPurDetInfo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select code_desc from default_code c, default_code_det d where d.id.companyCode = c.id.companyCode " +
            "and d.id.plantNo = c.id.plantNo and d.default_code = c.default_code and d.code_value = :uom " +
            "and c.id.companyCode = :companyCode and c.id.plantNo = :plantNo and c.default_code = 'INVENTORY.UOM' and c.db_status = 'Y'")
    List<Object[]> getUomDesc(String companyCode, Integer plantNo, String uom);*/

    @Query(value = "select count(pd) from PURDET pd where pd.id.companyCode = :companyCode " +
            "and pd.id.plantNo = :plantNo and pd.id.poNo = :poNo and pd.itemNo like %:itemNo% " +
            "and coalesce(pd.orderQty,0) > coalesce(pd.recdQty,0)")
    Long countItemNo(String companyCode, Integer plantNo, String poNo, String itemNo);

    @Query(value = "select count(pd) from PURDET pd where pd.id.companyCode = :companyCode " +
            "and pd.id.plantNo = :plantNo and pd.id.poNo = :poNo and pd.partNo like %:partNo% " +
            "and coalesce(pd.orderQty,0) > coalesce(pd.recdQty,0)")
    Long countPartNo(String companyCode, Integer plantNo, String poNo, String partNo);

    @Query(value = "select pd.id.recSeq, pd.partNo from PURDET pd where pd.id.companyCode = :companyCode " +
            "and pd.id.plantNo = :plantNo and pd.id.poNo = :poNo and pd.partNo like %:partNo% " +
            "and (pd.id.recSeq = :poRecSeq or :poRecSeq is null) " +
            "and coalesce(pd.orderQty,0) > coalesce(pd.recdQty,0)")
    List<PurDet> checkDuplicatePartNo(String companyCode, Integer plantNo, String poNo, String partNo, Integer poRecSeq);

    @Query(value = "SELECT pd.partNo, pd.id.recSeq, pd.itemNo, substring(coalesce(i.description,pd.remarks),1,60) , " +
            "i.mslCode, pd.itemType, pd.loc, pd.uom, pd.projectNo, coalesce(pd.orderQty,0) - coalesce(pd.recdQty,0), pd.unitPrice, " +
            "pd.dueDate, pd.resvQty, pd.invUom, pd.stdPackQty, pd.remarks FROM PURDET pd, ITEM i " +
            "WHERE i.companyCode = pd.id.companyCode AND i.plantNo = pd.id.plantNo AND i.itemNo = pd.itemNo " +
            "AND pd.id.companyCode =:companyCode AND pd.id.plantNo =:plantNo AND pd.id.poNo =:poNo " +
            "AND ( (:itemNo IS NULL ) AND ( pd.partNo LIKE %:partNo% ) " +
            "OR (:partNo IS NULL ) AND ( pd.itemNo LIKE %:itemNo% ) OR ( pd.itemNo LIKE %:itemNo%) AND (pd.partNo LIKE %:partNo% )) AND ( pd.id.recSeq =:poRecSeq " +
            "OR :poRecSeq IS NULL ) AND coalesce(pd.orderQty,0) > coalesce(pd.recdQty,0)")
    List<PurDet> getDataFromItemAndPartNo(String companyCode, Integer plantNo, String poNo, String itemNo, String partNo, Integer poRecSeq);

    @Query(value = "select distinct p.id.poNo, p.openClose " +
            "from PUR p, PURDET pd where p.id.companyCode = pd.id.companyCode and p.id.plantNo = pd.id.plantNo " +
            "and p.id.poNo = pd.id.poNo and p.id.companyCode = :companyCode and p.id.plantNo = :plantNo and p.id.poNo = :poNo")
    List<Pur> checkStatusPoNoPur(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select p.supplierCode, p.currencyCode, p.currencyRate, p.buyer, p.rlseDate, p.remarks from PUR p " +
            "where p.id.companyCode = :companyCode and p.id.plantNo = :plantNo and p.id.poNo = :poNo")
    List<Pur> getPurInfo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "SELECT p.id.poNo FROM PUR p WHERE p.id.companyCode = :companyCode " +
            "AND p.id.plantNo = :plantNo AND coalesce(p.openClose,'X') NOT IN ('V','Y') ORDER BY p.id.poNo")
    List<Pur> getAllPoNo(String companyCode, Integer plantNo);

    @Query(value = "select p.id.poNo, pd.id.recSeq from PUR p, PURDET pd where pd.id.companyCode = p.id.companyCode " +
            "and pd.id.plantNo = p.id.plantNo and (:itemType = 0 and pd.itemNo = :itemNo or :itemType = 1 and pd.partNo = :partNo) and pd.id.poNo = p.id.poNo " +
            "and p.id.companyCode = :companyCode and p.id.plantNo = :plantNo and p.id.poNo = :poNo")
    List<PurDet> getPoNoAndRecSeq(String companyCode, Integer plantNo, Integer itemType, String itemNo, String partNo, String poNo);
}
