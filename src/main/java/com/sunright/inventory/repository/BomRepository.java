package com.sunright.inventory.repository;

import com.sunright.inventory.entity.bom.Bom;
import com.sunright.inventory.entity.bom.BomId;
import com.sunright.inventory.entity.bom.BomProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomRepository extends JpaRepository<Bom, BomId>, JpaSpecificationExecutor<Bom> {

    @Query("select b.component as component from BOM b where b.id.companyCode = :companyCode " +
            "and b.id.plantNo = :plantNo and b.component = :itemNo")
    List<BomProjection> bomCur(String companyCode, Integer plantNo, String itemNo);
}
