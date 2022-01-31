package com.sunright.inventory.repository;

import com.sunright.inventory.entity.supplier.Supplier;
import com.sunright.inventory.entity.supplier.SupplierId;
import com.sunright.inventory.entity.supplier.SupplierProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, SupplierId>, JpaSpecificationExecutor<Supplier> {

    @Query(value = "select name from SUPPLIER where company_code = :companyCode " +
            "and plant_no = :plantNo and supplier_code = :supplierCode")
    Supplier getSupplierName(String companyCode, Integer plantNo, String supplierCode);

    @Query("SELECT s.id.supplierCode as supplierCode, s.name as name " +
            "FROM GRN g " +
            "    JOIN PUR p on g.companyCode = p.id.companyCode and g.plantNo = p.id.plantNo and g.poNo = p.id.poNo " +
            "    JOIN SUPPLIER s on p.id.companyCode = s.id.companyCode and p.id.plantNo = s.id.plantNo and p.supplierCode = s.id.supplierCode " +
            "where g.companyCode = :companyCode " +
            "and g.plantNo = :plantNo " +
            "and g.grnNo = :grnNo")
    SupplierProjection getSupplierByGrn(String companyCode, Integer plantNo, String grnNo);
}
