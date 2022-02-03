package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.supplier.Supplier;
import com.sunright.inventory.entity.supplier.SupplierId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, SupplierId>, JpaSpecificationExecutor<Supplier> {

    @Query("SELECT s.name as name FROM SUPPLIER s WHERE s.id.companyCode = :companyCode " +
            "AND s.id.plantNo = :plantNo AND s.id.supplierCode = :supplierCode")
    GrnSupplierProjection getSupplierName(String companyCode, Integer plantNo, String supplierCode);
}
