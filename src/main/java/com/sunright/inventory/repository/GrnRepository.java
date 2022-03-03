package com.sunright.inventory.repository;

import com.sunright.inventory.entity.grn.Grn;
import com.sunright.inventory.entity.grn.GrnDetailProjection;
import com.sunright.inventory.entity.grn.GrnSupplierProjection;
import com.sunright.inventory.entity.msr.MSRDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrnRepository extends JpaRepository<Grn, Long>, JpaSpecificationExecutor<Grn> {

    Optional<Grn> findGrnByGrnNo(String grnNo);

    @Query("SELECT g.grnNo as grnNo FROM GRN g WHERE g.companyCode = :companyCode AND g.plantNo = :plantNo AND g.grnNo = :grnNo")
    GrnDetailProjection findPoNoByGrnNo(String companyCode, Integer plantNo, String grnNo);

    @Query("SELECT g.id as grnId, g.grnNo as grnNo, s.id.supplierCode as supplierCode, s.name as name " +
            "FROM GRN g " +
            "    JOIN PUR p on g.companyCode = p.id.companyCode and g.plantNo = p.id.plantNo and g.poNo = p.id.poNo " +
            "    JOIN SUPPLIER s on p.id.companyCode = s.id.companyCode and p.id.plantNo = s.id.plantNo and p.supplierCode = s.id.supplierCode " +
            "where g.companyCode = :companyCode " +
            "and g.plantNo = :plantNo " +
            "and g.grnNo = :grnNo")
    GrnSupplierProjection getSupplierByGrn(String companyCode, Integer plantNo, String grnNo);
}
