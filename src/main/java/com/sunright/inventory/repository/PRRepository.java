package com.sunright.inventory.repository;

import com.sunright.inventory.entity.pr.PR;
import com.sunright.inventory.entity.pr.PRId;
import com.sunright.inventory.entity.pr.PRProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PRRepository extends JpaRepository<PR, PRId>, JpaSpecificationExecutor<PR> {

    @Query("SELECT pr FROM PR pr where pr.id.companyCode = :companyCode and pr.id.plantNo = :plantNo and pr.id.docmNo = :docmNo")
    PRProjection prRmk(String companyCode, Integer plantNo, String docmNo);
}
