package com.sunright.inventory.repository;

import com.sunright.inventory.entity.sale.SaleDetail;
import com.sunright.inventory.entity.sale.SaleDetailId;
import com.sunright.inventory.entity.sale.SaleDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, SaleDetailId>, JpaSpecificationExecutor<SaleDetail> {

    @Query("SELECT DISTINCT COALESCE(sdet.productType, 'T', 'S', sdet.productType) as productType " +
            "FROM SALEDET sdet WHERE sdet.id.companyCode = :companyCode AND sdet.id.plantNo = :plantNo " +
            "AND sdet.projectNoSub = :projectNo")
    SaleDetailProjection getProjectType(String companyCode, Integer plantNo, String projectNo);
}