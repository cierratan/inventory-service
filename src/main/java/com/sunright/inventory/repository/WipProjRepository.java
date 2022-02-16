package com.sunright.inventory.repository;

import com.sunright.inventory.entity.wip.WipProj;
import com.sunright.inventory.entity.wip.WipProjId;
import com.sunright.inventory.entity.wip.WipProjProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WipProjRepository extends JpaRepository<WipProj, WipProjId>, JpaSpecificationExecutor<WipProj> {

    @Query(value = "SELECT distinct w.project_no_sub as projectNoSub FROM wipproj w, saledet s WHERE s.company_code = :companyCode " +
            "and s.plant_no = :plantNo and s.order_no = w.order_no AND s.project_no_sub = w.project_no_sub AND w.company_code = :companyCode and w.plant_no = :plantNo " +
            "and w.order_no = :docmNo AND w.project_no_sub = :projectNoSub union SELECT distinct w.project_no_sub" +
            "FROM wipproj w, saledet s WHERE s.company_code = :companyCode and s.plant_no = :plantNo and s.order_no = w.order_no " +
            "AND s.project_no_sub = w.project_no_sub AND w.company_code = :companyCode and w.plant_no = :plantNo and w.project_no_sub = :projectNoSub", nativeQuery = true)
    List<WipProjProjection> projectCur(String companyCode, Integer plantNo, String docmNo, String projectNoSub);

    @Query("SELECT DISTINCT w.id.orderNo as orderNo FROM WIPPROJ w JOIN SALEDET s ON s.id.orderNo = w.id.orderNo AND s.projectNoSub = w.id.projectNoSub " +
            "WHERE s.id.companyCode=:companyCode AND s.id.plantNo=:plantNo AND w.id.companyCode=:companyCode AND w.id.plantNo=:plantNo " +
            "AND w.id.orderNo=:docmNo AND w.id.projectNoSub=:projectNoSub")
    WipProjProjection wpCur(String companyCode, Integer plantNo, String docmNo, String projectNoSub);

    @Query("SELECT DISTINCT w.id.projectNoSub as projectNoSub FROM WIPPROJ w JOIN SALEDET s ON s.id.orderNo = w.id.orderNo AND s.projectNoSub = w.id.projectNoSub " +
            "WHERE s.id.companyCode=:companyCode AND s.id.plantNo=:plantNo AND w.id.companyCode=:companyCode AND w.id.plantNo=:plantNo " +
            "AND w.id.projectNoSub=:projectNoSub")
    WipProjProjection prCur(String companyCode, Integer plantNo, String projectNoSub);
}
