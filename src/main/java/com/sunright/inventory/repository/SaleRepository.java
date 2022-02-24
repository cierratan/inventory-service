package com.sunright.inventory.repository;

import com.sunright.inventory.entity.sale.Sale;
import com.sunright.inventory.entity.sale.SaleId;
import com.sunright.inventory.entity.sale.SaleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, SaleId>, JpaSpecificationExecutor<Sale> {

    @Query("SELECT s.coqDivCode as coqDivCode, s.coqDeptCode as coqDeptCode, cr.id.reasonCode as reasonCode, cr.reasonDesc as reasonDesc " +
            "FROM SALE s join COQ_REASONS_DET cr on cr.id.reasonCode = s.coqReasonCode AND cr.id.catCode = s.coqCatCode " +
            "WHERE cr.id.companyCode = :companyCode AND cr.id.plantNo = :plantNo " +
            "AND cr.id.docmType in ('WO') AND s.id.companyCode = :companyCode " +
            "AND s.id.plantNo = :plantNo AND s.id.orderNo = :docmNo")
    SaleProjection saleCoqReasonsDet(String companyCode, Integer plantNo, String docmNo);

    @Query(value = "select s.coq_div_code as coqDivCode, s.coq_dept_code as coqDeptCode, cr.reason_code as reasonCode, cr.reason_desc as reasonDesc " +
            "from sale s," +
            "     coq_reasons_det cr " +
            "where cr.company_code = :companyCode" +
            "  and cr.plant_no = :plantNo" +
            "  and cr.docm_type = :WKType" +
            "  and cr.reason_code = s.coq_reason_code" +
            "  and cr.cat_code = s.coq_cat_code" +
            "  and s.company_code = :companyCode" +
            "  and s.plant_no = :plantNo" +
            "  and s.order_no = :docmNo " +
            "union " +
            "select p.div_code as divCode, p.dept_code as deptCode, pd.reason_code as reasonCode, pd.reason_desc as reasonDesc " +
            "from pr p," +
            "     prdet pd " +
            "where pd.company_code = :companyCode" +
            "  and pd.plant_no = :plantNo" +
            "  and pd.item_type = 0" +
            "  and pd.docm_no = p.docm_no" +
            "  and p.company_code = :companyCode" +
            "  and p.plant_no = :plantNo" +
            "  and p.docm_type = :PRType" +
            "  and p.docm_no = :docmNo", nativeQuery = true)
    List<SaleProjection> cCoqInfo(String companyCode, Integer plantNo, String docmNo, String WKType, String PRType);

    @Query("SELECT s.id.orderNo as orderNo, s.openClose as openClose " +
            "FROM SALE s WHERE s.id.companyCode = :companyCode AND s.id.plantNo = :plantNo AND s.id.orderNo = :docmNo")
    SaleProjection cOrder(String companyCode, Integer plantNo, String docmNo);
}