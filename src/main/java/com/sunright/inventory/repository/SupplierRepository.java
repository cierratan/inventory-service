package com.sunright.inventory.repository;

import com.sunright.inventory.entity.supplier.Supplier;
import com.sunright.inventory.entity.supplier.SupplierId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends PagingAndSortingRepository<Supplier, SupplierId>, JpaSpecificationExecutor<Supplier> {

    @Query(value = "select name from supplier where company_code = :companyCode " +
            "and plant_no = :plantNo and supplier_code = :supplierCode", nativeQuery = true)
    List<Object[]> getSupplierName(String companyCode, Integer plantNo, String supplierCode);

}
