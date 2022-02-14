package com.sunright.inventory.repository;

import com.sunright.inventory.entity.sale.Sale;
import com.sunright.inventory.entity.sale.SaleId;
import com.sunright.inventory.entity.sale.SaleProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, SaleId>, JpaSpecificationExecutor<Sale> {

    @Query("SELECT s.coqDivCode as coqDivCode, s.coqDeptCode as coqDeptCode, cr.id.reasonCode as reasonCode, cr.reasonDesc as reasonDesc " +
            "FROM SALE s join COQ_REASONS_DET cr on cr.id.reasonCode = s.coqReasonCode AND cr.id.catCode = s.coqCatCode " +
            "WHERE cr.id.companyCode = :companyCode AND cr.id.plantNo = :plantNo " +
            "AND cr.id.docmType in ('WO') AND s.id.companyCode = :companyCode " +
            "AND s.id.plantNo = :plantNo AND s.id.orderNo = :docmNo")
    SaleProjection saleCoqReasonsDet(String companyCode, Integer plantNo, String docmNo);
}