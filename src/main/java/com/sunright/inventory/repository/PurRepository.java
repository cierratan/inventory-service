package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pur.Pur;
import com.sunright.inventory.entity.pur.PurId;
import com.sunright.inventory.entity.pur.PurProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurRepository extends JpaRepository<Pur, PurId>, JpaSpecificationExecutor<Pur> {

    @Query("SELECT DISTINCT p.id.poNo as poNo, p.openClose as openClose " +
            "FROM PUR p join PURDET pd on p.id.companyCode = pd.id.companyCode " +
            "AND p.id.plantNo = pd.id.plantNo AND p.id.poNo = pd.id.poNo " +
            "WHERE p.id.companyCode = :companyCode AND p.id.plantNo = :plantNo AND p.id.poNo = :poNo")
    PurProjection checkStatusPoNoPur(String companyCode, Integer plantNo, String poNo);

    @Query("SELECT p.supplierCode as supplierCode, p.currencyCode as currencyCode , " +
            "p.currencyRate as currencyRate, p.buyer as buyer, " +
            "p.rlseDate as rlseDate, p.remarks as remarks FROM PUR p " +
            "WHERE p.id.companyCode = :companyCode AND p.id.plantNo = :plantNo AND p.id.poNo = :poNo")
    PurProjection getPurInfo(String companyCode, Integer plantNo, String poNo);

    @Query("SELECT pur.id.poNo as poNo FROM PUR pur WHERE pur.id.companyCode=:companyCode " +
            "AND pur.id.plantNo=:plantNo AND COALESCE(pur.openClose,'X') NOT IN ('V','Y') ORDER BY pur.id.poNo")
    List<PurProjection> getAllPoNo(String companyCode, Integer plantNo);
}