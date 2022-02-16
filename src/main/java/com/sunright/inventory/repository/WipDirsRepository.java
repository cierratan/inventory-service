package com.sunright.inventory.repository;

import com.sunright.inventory.entity.wip.WipDirs;
import com.sunright.inventory.entity.wip.WipDirsId;
import com.sunright.inventory.entity.wip.WipDirsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WipDirsRepository extends JpaRepository<WipDirs, WipDirsId>, JpaSpecificationExecutor<WipDirs> {

    @Query("SELECT DISTINCT w.id.orderNo as orderNo FROM WIPDIRS w join SALEDET s on s.id.orderNo = w.id.orderNo " +
            "WHERE s.id.companyCode=:companyCode AND s.id.plantNo=:plantNo AND w.id.companyCode=:companyCode AND w.id.plantNo=:plantNo " +
            "AND w.id.orderNo=:projectNo")
    List<WipDirsProjection> orderCur(String companyCode, Integer plantNo, String projectNo);
}
