package com.sunright.inventory.repository;

import com.sunright.inventory.entity.Pur;
import com.sunright.inventory.entity.PurId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurRepository extends PagingAndSortingRepository<Pur, PurId>, JpaSpecificationExecutor<Pur> {

    @Query(value = "select po_no,decode(open_close,'A', decode(s1.in_transit, 0, 'C', 'A'),open_close) open_close " +
            "from s_pur, (select sum(order_qty - nvl(recd_qty,0)) in_transit from s_purdet " +
            "where company_code = :companyCode and plant_no = :plantNo and po_no = :poNo) s1 where company_code = :companyCode " +
            "and plant_no = :plantNo and po_no = :poNo", nativeQuery = true)
    List<Object[]> checkStatusPoNoPur(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select supplier_code,currency_code,buyer,rlse_date,remarks " +
            "from s_pur where company_code = :companyCode and plant_no = :plantNo and po_no = :poNo", nativeQuery = true)
    List<Object[]> getPurInfo(String companyCode, Integer plantNo, String poNo);

    @Query(value = "select name from s_supplier where company_code = :companyCode " +
            "and plant_no = :plantNo and supplier_code = :supplierCode", nativeQuery = true)
    List<Object[]> getSupplierName(String companyCode, Integer plantNo, String supplierCode);
}
