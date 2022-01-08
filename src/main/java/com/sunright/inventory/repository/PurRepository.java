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
}
